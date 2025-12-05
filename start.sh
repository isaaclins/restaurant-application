#!/bin/bash
#
# Author: Isaac Lins
# Description: Start the backend, client app, serve the website or do all
# Usage: ./start.sh [OPTIONS]
#
# Options:
#   --backend   Start all backend microservices (Docker + Spring Boot)
#   --client    Start the Tauri desktop client
#   --website   Start the React website dev server
#   --infra     Start only infrastructure (Docker: MySQL, Redis, Kafka)
#   --all       Start everything
#   --full      Full reset: stop all, delete DB volumes, restart everything
#   --test      Run tests as if in CI/CD pipeline
#   --stop      Stop all running services
#   --help      Show this help message
#
# Examples:
#   ./start.sh --backend --website
#   ./start.sh --client
#   ./start.sh --all
#   ./start.sh --infra
#   ./start.sh --test
#   ./start.sh --stop

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

# ============================================================================
# Load Environment Variables from .env file
# ============================================================================

load_env() {
    local env_file="$PROJECT_ROOT/.env"
    
    if [ -f "$env_file" ]; then
        print_info "Loading environment variables from .env..."
        
        # Export all variables from .env file (ignore comments and empty lines)
        set -a
        while IFS='=' read -r key value; do
            # Skip comments and empty lines
            if [[ ! "$key" =~ ^# && -n "$key" ]]; then
                # Remove leading/trailing whitespace and quotes
                key=$(echo "$key" | xargs)
                value=$(echo "$value" | xargs)
                # Remove surrounding quotes if present
                value="${value%\"}"
                value="${value#\"}"
                value="${value%\'}"
                value="${value#\'}"
                export "$key=$value"
            fi
        done < <(grep -v '^#' "$env_file" | grep -v '^$' | grep '=')
        set +a
        
        print_success "Environment variables loaded"
    else
        print_warning ".env file not found at $env_file"
        print_info "Copy .env.example to .env and configure your settings"
    fi
}

# Flags
START_BACKEND=false
START_CLIENT=false
START_WEBSITE=false
START_INFRA=false
STOP_ALL=false
RUN_TESTS=false
FULL_RESET=false

# ============================================================================
# Helper Functions
# ============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${BLUE}→ $1${NC}"
}

show_help() {
    echo "Restaurant Application Starter"
    echo ""
    echo "Usage: ./start.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --backend   Start all backend microservices (Docker + Spring Boot)"
    echo "  --client    Start the Tauri desktop client"
    echo "  --website   Start the React website dev server"
    echo "  --infra     Start only infrastructure (Docker: MySQL, Redis, Kafka)"
    echo "  --all       Start everything"
    echo "  --full      Full reset: stop all, delete DB volumes, restart everything"
    echo "  --test      Run tests as if in CI/CD pipeline"
    echo "  --stop      Stop all running services"
    echo "  --help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./start.sh --backend --website"
    echo "  ./start.sh --client"
    echo "  ./start.sh --all"
    echo "  ./start.sh --test"
    echo "  ./start.sh --stop"
}

check_requirements() {
    print_header "Checking Requirements"
    
    local missing=false
    
    # Check Docker
    if command -v docker &> /dev/null; then
        print_success "Docker found"
    else
        print_error "Docker not found - required for backend"
        missing=true
    fi
    
    # Check Docker Compose
    if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
        print_success "Docker Compose found"
    else
        print_error "Docker Compose not found - required for backend"
        missing=true
    fi
    
    # Check Node.js (for website)
    if command -v node &> /dev/null; then
        print_success "Node.js found ($(node --version))"
    else
        print_warning "Node.js not found - required for website"
    fi
    
    # Check npm
    if command -v npm &> /dev/null; then
        print_success "npm found"
    else
        print_warning "npm not found - required for website"
    fi
    
    # Check Rust/Cargo (for Tauri client)
    if command -v cargo &> /dev/null; then
        print_success "Cargo found (Rust)"
    else
        print_warning "Cargo not found - required for Tauri client"
    fi
    
    # Check Java (for Spring Boot)
    if command -v java &> /dev/null; then
        print_success "Java found ($(java --version 2>&1 | head -1))"
    else
        print_warning "Java not found - required for backend services"
    fi
    
    if [ "$missing" = true ]; then
        print_error "Missing required dependencies. Please install them first."
        exit 1
    fi
    
    echo ""
}

# ============================================================================
# Infrastructure (Docker)
# ============================================================================

start_infrastructure() {
    print_header "Starting Infrastructure (Docker)"
    
    cd "$PROJECT_ROOT"
    
    if [ -f "docker-compose.yml" ]; then
        print_info "Starting Docker containers..."
        docker compose up -d
        
        print_info "Waiting for services to be ready..."
        sleep 5
        
        print_success "Infrastructure started"
        echo ""
        echo "  Services:"
        echo "  • MySQL:     localhost:3306"
        echo "  • Redis:     localhost:6379"
        echo "  • Kafka:     localhost:9092"
        echo "  • Zookeeper: localhost:2181"
    else
        print_warning "docker-compose.yml not found. Creating basic infrastructure..."
        print_info "Run './start.sh --infra' again after docker-compose.yml is created"
    fi
}

stop_infrastructure() {
    print_info "Stopping Docker containers..."
    cd "$PROJECT_ROOT"
    
    if [ -f "docker-compose.yml" ]; then
        docker compose down
        print_success "Infrastructure stopped"
    fi
}

# ============================================================================
# Backend Services
# ============================================================================

# Helper function to wait for a service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=${3:-90}
    local attempt=0
    
    print_info "Waiting for $service_name (port $port) to be ready..."
    while ! curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; do
        attempt=$((attempt + 1))
        if [ $attempt -ge $max_attempts ]; then
            print_error "$service_name failed to start within $max_attempts seconds"
            return 1
        fi
        sleep 1
        # Show progress every 10 seconds
        if [ $((attempt % 10)) -eq 0 ]; then
            print_info "  Still waiting for $service_name... ($attempt/$max_attempts)"
        fi
    done
    print_success "$service_name is ready!"
    return 0
}

# Helper function to start a service and optionally wait for it
start_service() {
    local service_name=$1
    local wait_for_ready=${2:-false}
    local port=$3
    
    if [ -d "$PROJECT_ROOT/backend/$service_name" ]; then
        print_info "Starting $service_name..."
        cd "$PROJECT_ROOT/backend/$service_name"
        
        if [ -f "mvnw" ]; then
            ./mvnw spring-boot:run &
        elif [ -f "gradlew" ]; then
            ./gradlew bootRun &
        elif [ -f "pom.xml" ]; then
            mvn spring-boot:run &
        else
            print_warning "$service_name: No build file found, skipping..."
            return 1
        fi
        
        if [ "$wait_for_ready" = true ] && [ -n "$port" ]; then
            wait_for_service "$service_name" "$port"
        fi
    else
        print_warning "$service_name directory not found, skipping..."
        return 1
    fi
}

start_backend() {
    print_header "Starting Backend Services"
    
    # First start infrastructure
    start_infrastructure
    
    if [ ! -d "$PROJECT_ROOT/backend" ]; then
        print_warning "Backend directory not found. Skipping..."
        return
    fi
    
    print_header "Phase 1: Core Infrastructure Services"
    
    # 1. Eureka Server - Service Discovery (all services depend on this)
    start_service "eureka-server" true 8761 || return 1
    
    # 2. Settings Service - Configuration (other services may need settings)
    start_service "settings-service" true 8087 || return 1
    
    # 3. Auth Service - Authentication (gateway needs this for security)
    start_service "auth-service" true 8085 || return 1
    
    print_header "Phase 2: Data Services"
    
    # 4. Product Service - Master data (orders reference products)
    start_service "product-service" true 8081 || return 1
    
    print_header "Phase 3: API Gateway"
    
    # 5. API Gateway - Routing (needs services registered in Eureka)
    start_service "api-gateway" true 8080 || return 1
    
    print_header "Phase 4: Business Services"
    
    # 6. Cart, Order, Payment - Can start in parallel now
    start_service "cart-service" false
    start_service "order-service" false
    start_service "payment-service" false
    
    # Wait for order-service specifically (receipt depends on it)
    wait_for_service "order-service" 8083
    
    print_header "Phase 5: Dependent Services"
    
    # 7. Receipt & Notification - Depend on order events
    start_service "receipt-service" false
    start_service "notification-service" false
    
    # Final readiness check
    print_header "Verifying All Services"
    sleep 5
    
    local all_ready=true
    local service_ports=(
        "eureka-server:8761"
        "api-gateway:8080"
        "product-service:8081"
        "cart-service:8082"
        "order-service:8083"
        "payment-service:8084"
        "auth-service:8085"
        "receipt-service:8086"
        "settings-service:8087"
    )
    
    for service_port in "${service_ports[@]}"; do
        local svc="${service_port%%:*}"
        local port="${service_port##*:}"
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            print_success "$svc (port $port) ✓"
        else
            print_warning "$svc (port $port) not responding yet"
            all_ready=false
        fi
    done
    
    echo ""
    if [ "$all_ready" = true ]; then
        print_success "All backend services are ready!"
    else
        print_warning "Some services are still starting. They should be ready shortly."
    fi
    
    echo ""
    echo "  ┌─────────────────────────────────────────────────────────┐"
    echo "  │                    SERVICE ENDPOINTS                    │"
    echo "  ├─────────────────────────────────────────────────────────┤"
    echo "  │  Eureka Dashboard:    http://localhost:8761             │"
    echo "  │  API Gateway:         http://localhost:8080             │"
    echo "  ├─────────────────────────────────────────────────────────┤"
    echo "  │  Products API:        http://localhost:8080/api/products│"
    echo "  │  Cart API:            http://localhost:8080/api/cart    │"
    echo "  │  Orders API:          http://localhost:8080/api/orders  │"
    echo "  │  Payments API:        http://localhost:8080/api/payments│"
    echo "  │  Auth API:            http://localhost:8080/api/auth    │"
    echo "  │  Receipts API:        http://localhost:8080/api/receipts│"
    echo "  │  Settings API:        http://localhost:8080/api/settings│"
    echo "  └─────────────────────────────────────────────────────────┘"
    echo ""
}

stop_backend() {
    print_info "Stopping backend services..."
    
    # Kill Spring Boot processes
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "bootRun" 2>/dev/null || true
    
    print_success "Backend services stopped"
}

# ============================================================================
# Website (React)
# ============================================================================

start_website() {
    print_header "Starting Website (React)"
    
    if [ ! -d "$PROJECT_ROOT/website" ]; then
        print_warning "Website directory not found. Skipping..."
        return
    fi
    
    cd "$PROJECT_ROOT/website"
    
    # Check if package.json exists
    if [ ! -f "package.json" ]; then
        print_warning "Website package.json not found. Skipping..."
        return
    fi
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        print_info "Installing dependencies..."
        npm install
    fi
    
    print_info "Starting development server..."
    npm run dev &
    
    sleep 3
    print_success "Website started"
    echo ""
    echo "  URL: http://localhost:5173"
}

stop_website() {
    print_info "Stopping website..."
    
    # Kill Vite dev server
    pkill -f "vite" 2>/dev/null || true
    
    print_success "Website stopped"
}

# ============================================================================
# Client (Tauri)
# ============================================================================

start_client() {
    print_header "Starting Client (Tauri)"
    
    if [ ! -d "$PROJECT_ROOT/client" ]; then
        print_warning "Client directory not found. Skipping..."
        return
    fi
    
    cd "$PROJECT_ROOT/client"
    
    # Check if package.json exists
    if [ ! -f "package.json" ]; then
        print_warning "Client package.json not found. Skipping..."
        return
    fi
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        print_info "Installing dependencies..."
        npm install
    fi
    
    print_info "Starting Tauri development mode..."
    npm run tauri dev &
    
    print_success "Client starting..."
    echo ""
    echo "  Tauri app will open automatically"
}

stop_client() {
    print_info "Stopping client..."
    
    # Kill Tauri processes
    pkill -f "tauri" 2>/dev/null || true
    
    print_success "Client stopped"
}

# ============================================================================
# Run Tests (CI/CD Pipeline Mode)
# ============================================================================

run_tests() {
    print_header "Running Tests (CI/CD Pipeline Mode)"
    
    local test_failed=false
    
    # Backend tests
    if [ -d "$PROJECT_ROOT/backend" ]; then
        print_info "Running backend tests..."
        
        local services=("eureka-server" "api-gateway" "product-service" "cart-service" "order-service" "payment-service" "auth-service" "settings-service" "receipt-service")
        
        for service in "${services[@]}"; do
            if [ -d "$PROJECT_ROOT/backend/$service" ]; then
                print_info "Testing $service..."
                cd "$PROJECT_ROOT/backend/$service"
                
                if [ -f "mvnw" ]; then
                    if ./mvnw test; then
                        print_success "$service tests passed"
                    else
                        print_error "$service tests failed"
                        test_failed=true
                    fi
                elif [ -f "pom.xml" ]; then
                    if mvn test; then
                        print_success "$service tests passed"
                    else
                        print_error "$service tests failed"
                        test_failed=true
                    fi
                else
                    print_warning "$service: No test configuration found, skipping..."
                fi
            fi
        done
    fi
    
    # Client tests
    if [ -d "$PROJECT_ROOT/client" ]; then
        print_info "Running client tests..."
        cd "$PROJECT_ROOT/client"
        
        if [ -f "package.json" ]; then
            # Install dependencies if needed
            if [ ! -d "node_modules" ]; then
                print_info "Installing client dependencies..."
                npm install
            fi
            
            # Check if test script exists
            if npm run 2>/dev/null | grep -q "test"; then
                if npm run test -- --passWithNoTests || npm test -- --passWithNoTests; then
                    print_success "Client tests passed"
                else
                    print_error "Client tests failed"
                    test_failed=true
                fi
            else
                print_warning "No test script found in client, skipping..."
            fi
        fi
    fi
    
    # Website tests
    if [ -d "$PROJECT_ROOT/website" ]; then
        print_info "Running website tests..."
        cd "$PROJECT_ROOT/website"
        
        if [ -f "package.json" ]; then
            # Install dependencies if needed
            if [ ! -d "node_modules" ]; then
                print_info "Installing website dependencies..."
                npm install
            fi
            
            # Check if test script exists
            if npm run 2>/dev/null | grep -q "test"; then
                if npm run test -- --passWithNoTests || npm test -- --passWithNoTests; then
                    print_success "Website tests passed"
                else
                    print_error "Website tests failed"
                    test_failed=true
                fi
            else
                print_warning "No test script found in website, skipping..."
            fi
        fi
    fi

    # Client E2E tests
    if [ -d "$PROJECT_ROOT/client" ]; then
        print_info "Running client E2E tests..."
        cd "$PROJECT_ROOT/client"
        
        if [ -f "package.json" ]; then
            if npm run 2>/dev/null | grep -q "test:e2e"; then
                # Check if services are running
                local backend_ready=false
                local frontend_ready=false
                
                if nc -z localhost 8080 2>/dev/null; then
                    backend_ready=true
                fi
                
                if nc -z localhost 5173 2>/dev/null; then
                    frontend_ready=true
                fi
                
                if [ "$backend_ready" = true ] && [ "$frontend_ready" = true ]; then
                    print_info "Backend and Frontend are running. Starting Cypress..."
                    if npm run test:e2e; then
                        print_success "Client E2E tests passed"
                    else
                        print_error "Client E2E tests failed"
                        test_failed=true
                    fi
                else
                    print_warning "Skipping E2E tests because application is not running."
                    print_warning "  Backend (8080): $([ "$backend_ready" = true ] && echo "UP" || echo "DOWN")"
                    print_warning "  Frontend (5173): $([ "$frontend_ready" = true ] && echo "UP" || echo "DOWN")"
                    print_info "  Run './start.sh --all' in another terminal before running tests."
                fi
            fi
        fi
    fi
    
    echo ""
    if [ "$test_failed" = true ]; then
        print_error "Some tests failed!"
        exit 1
    else
        print_success "All tests passed!"
    fi
}

# ============================================================================
# Stop All
# ============================================================================

stop_all() {
    print_header "Stopping All Services"
    
    stop_client
    stop_website
    stop_backend
    stop_infrastructure
    
    print_success "All services stopped"
}

# ============================================================================
# Full Reset (Stop, Delete DB, Restart)
# ============================================================================

full_reset() {
    print_header "🔄 Full Reset - Resetting Everything"
    
    # Stop all services first
    print_info "Stopping all services..."
    stop_client
    stop_website
    stop_backend
    
    # Stop infrastructure and delete volumes
    print_info "Stopping infrastructure and deleting database volumes..."
    cd "$PROJECT_ROOT"
    docker-compose down -v 2>/dev/null || docker compose down -v 2>/dev/null || true
    
    print_success "All data volumes deleted (database reset)"
    
    # Wait a moment for cleanup
    sleep 2
    
    # Restart everything
    print_header "🚀 Starting Fresh"
    
    check_requirements
    load_env
    
    # Start infrastructure (will recreate volumes)
    start_infrastructure
    
    # Start backend
    start_backend
    
    # Start client
    start_client
    
    print_header "✅ Full Reset Complete"
    echo "All services are running with a fresh database."
    echo ""
    echo "  Default admin login:"
    echo "  • Email:    admin@restaurant.com"
    echo "  • Password: admin123"
    echo ""
}

# ============================================================================
# Parse Arguments
# ============================================================================

if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

while [[ $# -gt 0 ]]; do
    case $1 in
        --backend)
            START_BACKEND=true
            shift
            ;;
        --client)
            START_CLIENT=true
            shift
            ;;
        --website)
            START_WEBSITE=true
            shift
            ;;
        --infra)
            START_INFRA=true
            shift
            ;;
        --all)
            START_BACKEND=true
            START_CLIENT=true
            START_WEBSITE=true
            shift
            ;;
        --test)
            RUN_TESTS=true
            shift
            ;;
        --full)
            FULL_RESET=true
            shift
            ;;
        --stop)
            STOP_ALL=true
            shift
            ;;
        --help|-h)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
done

# ============================================================================
# Main Execution
# ============================================================================

print_header "🍕 Restaurant Application Starter"

if [ "$STOP_ALL" = true ]; then
    stop_all
    exit 0
fi

if [ "$FULL_RESET" = true ]; then
    full_reset
    exit 0
fi

if [ "$RUN_TESTS" = true ]; then
    run_tests
    exit 0
fi

check_requirements

# Load environment variables from .env file
load_env

if [ "$START_INFRA" = true ]; then
    start_infrastructure
fi

if [ "$START_BACKEND" = true ]; then
    start_backend
fi

if [ "$START_WEBSITE" = true ]; then
    start_website
fi

if [ "$START_CLIENT" = true ]; then
    start_client
fi

print_header "🚀 Startup Complete"
echo "Use './start.sh --stop' to stop all services"
echo "" 

