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
#   -s, --silent  Silent mode: minimal output, only show final results
#   --help      Show this help message
#
# Examples:
#   ./start.sh --backend --website
#   ./start.sh --client
#   ./start.sh --all
#   ./start.sh --test -s        # Silent testing
#   ./start.sh --full -s        # Silent full reset
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
SILENT=false

# ============================================================================
# Helper Functions
# ============================================================================

print_header() {
    [ "$SILENT" = true ] && return
    echo ""
    echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
    echo ""
}

print_success() {
    [ "$SILENT" = true ] && return
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    [ "$SILENT" = true ] && return
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    # Always show errors, even in silent mode
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    [ "$SILENT" = true ] && return
    echo -e "${BLUE}→ $1${NC}"
}

# Print final results (always shown, even in silent mode)
print_final() {
    echo -e "$1"
}

show_help() {
    echo "Restaurant Application Starter"
    echo ""
    echo "Usage: ./start.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --backend     Start all backend microservices (Docker + Spring Boot)"
    echo "  --client      Start the Tauri desktop client"
    echo "  --website     Start the React website dev server"
    echo "  --infra       Start only infrastructure (Docker: MySQL, Redis, Kafka)"
    echo "  --all         Start everything"
    echo "  --full        Full reset: stop all, delete DB volumes, restart everything"
    echo "  --test        Run tests as if in CI/CD pipeline"
    echo "  --stop        Stop all running services"
    echo "  -s, --silent  Silent mode: minimal output, only errors and final results"
    echo "  --help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./start.sh --backend --website"
    echo "  ./start.sh --client"
    echo "  ./start.sh --all"
    echo "  ./start.sh --test -s       # Silent testing"
    echo "  ./start.sh --full -s       # Silent full reset"
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
    
    [ "$SILENT" = false ] && echo ""
}

# ============================================================================
# Infrastructure (Docker)
# ============================================================================

start_infrastructure() {
    print_header "Starting Infrastructure (Docker)"
    
    cd "$PROJECT_ROOT"
    
    if [ -f "docker-compose.yml" ]; then
        print_info "Starting Docker containers..."
        if [ "$SILENT" = true ]; then
            docker compose up -d --quiet-pull 2>/dev/null
        else
            docker compose up -d
        fi
        
        print_info "Waiting for services to be ready..."
        sleep 5
        
        print_success "Infrastructure started"
        if [ "$SILENT" = false ]; then
            echo ""
            echo "  Services:"
            echo "  • MySQL:     localhost:3306"
            echo "  • Redis:     localhost:6379"
            echo "  • Kafka:     localhost:9092"
            echo "  • Zookeeper: localhost:2181"
        fi
    else
        print_warning "docker-compose.yml not found. Creating basic infrastructure..."
        print_info "Run './start.sh --infra' again after docker-compose.yml is created"
    fi
}

stop_infrastructure() {
    print_info "Stopping Docker containers..."
    cd "$PROJECT_ROOT"
    
    if [ -f "docker-compose.yml" ]; then
        if [ "$SILENT" = true ]; then
            docker compose down 2>/dev/null
        else
            docker compose down
        fi
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
        # Show progress every 10 seconds (only in non-silent mode)
        if [ "$SILENT" = false ] && [ $((attempt % 10)) -eq 0 ]; then
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
        
        if [ "$SILENT" = true ]; then
            # Silent mode: redirect all output to /dev/null
            if [ -f "mvnw" ]; then
                ./mvnw spring-boot:run -q > /dev/null 2>&1 &
            elif [ -f "gradlew" ]; then
                ./gradlew bootRun -q > /dev/null 2>&1 &
            elif [ -f "pom.xml" ]; then
                mvn spring-boot:run -q > /dev/null 2>&1 &
            else
                print_warning "$service_name: No build file found, skipping..."
                return 1
            fi
        else
            # Normal mode: show output
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
    
    if [ "$SILENT" = false ]; then
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
    fi
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
        if [ "$SILENT" = true ]; then
            npm install --silent 2>/dev/null
        else
            npm install
        fi
    fi
    
    print_info "Starting development server..."
    if [ "$SILENT" = true ]; then
        npm run dev > /dev/null 2>&1 &
    else
        npm run dev &
    fi
    
    sleep 3
    print_success "Website started"
    if [ "$SILENT" = false ]; then
        echo ""
        echo "  URL: http://localhost:5173"
    fi
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
        if [ "$SILENT" = true ]; then
            npm install --silent 2>/dev/null
        else
            npm install
        fi
    fi
    
    print_info "Starting Tauri development mode..."
    if [ "$SILENT" = true ]; then
        npm run tauri dev > /dev/null 2>&1 &
    else
        npm run tauri dev &
    fi
    
    print_success "Client starting..."
    if [ "$SILENT" = false ]; then
        echo ""
        echo "  Tauri app will open automatically"
    fi
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
    print_header "🧪 Running FULL Test Suite (CI/CD Pipeline Mode)"
    
    local test_failed=false
    local started_backend=false
    local started_frontend=false
    local frontend_pid=""
    
    # ============================================================================
    # PHASE 1: Backend Unit Tests
    # ============================================================================
    print_header "Phase 1: Backend Unit Tests"
    
    if [ -d "$PROJECT_ROOT/backend" ]; then
        local services=("eureka-server" "api-gateway" "product-service" "cart-service" "order-service" "payment-service" "auth-service" "settings-service" "receipt-service")
        
        for service in "${services[@]}"; do
            if [ -d "$PROJECT_ROOT/backend/$service" ]; then
                print_info "Testing $service..."
                cd "$PROJECT_ROOT/backend/$service"
                
                if [ "$SILENT" = true ]; then
                    # Silent mode: redirect ALL output to /dev/null
                    if [ -f "mvnw" ]; then
                        if ./mvnw test -q > /dev/null 2>&1; then
                            : # success, do nothing (silent)
                        else
                            print_error "$service tests failed"
                            test_failed=true
                        fi
                    elif [ -f "pom.xml" ]; then
                        if mvn test -q > /dev/null 2>&1; then
                            : # success, do nothing (silent)
                        else
                            print_error "$service tests failed"
                            test_failed=true
                        fi
                    fi
                else
                    # Normal mode: show output
                    if [ -f "mvnw" ]; then
                        if ./mvnw test -q; then
                            print_success "$service tests passed"
                        else
                            print_error "$service tests failed"
                            test_failed=true
                        fi
                    elif [ -f "pom.xml" ]; then
                        if mvn test -q; then
                            print_success "$service tests passed"
                        else
                            print_error "$service tests failed"
                            test_failed=true
                        fi
                    else
                        print_warning "$service: No test configuration found, skipping..."
                    fi
                fi
            fi
        done
    else
        print_warning "Backend directory not found, skipping backend tests..."
    fi
    
    # ============================================================================
    # PHASE 2: Start Infrastructure & Services for E2E Tests
    # ============================================================================
    print_header "Phase 2: Starting Services for E2E Tests"
    
    # Check if backend is already running
    if ! nc -z localhost 8080 2>/dev/null; then
        print_info "Starting backend services..."
        cd "$PROJECT_ROOT"
        start_backend
        started_backend=true
        
        # Wait for backend to be fully ready
        print_info "Waiting for backend to be fully ready..."
        local attempts=0
        while ! curl -s "http://localhost:8080/actuator/health" > /dev/null 2>&1; do
            attempts=$((attempts + 1))
            if [ $attempts -ge 120 ]; then
                print_error "Backend failed to start within 2 minutes"
                test_failed=true
                break
            fi
            sleep 1
            if [ $((attempts % 15)) -eq 0 ]; then
                print_info "  Still waiting for backend... ($attempts/120)"
            fi
        done
        
        if [ $attempts -lt 120 ]; then
            print_success "Backend is ready!"
        fi
    else
        print_success "Backend already running on port 8080"
    fi
    
    # Check if frontend is already running
    if ! nc -z localhost 1420 2>/dev/null; then
        print_info "Starting frontend dev server..."
        cd "$PROJECT_ROOT/client"
        
        # Install dependencies if needed
        if [ ! -d "node_modules" ]; then
            print_info "Installing client dependencies..."
            if [ "$SILENT" = true ]; then
                npm install --silent 2>/dev/null
            else
                npm install
            fi
        fi
        
        if [ "$SILENT" = true ]; then
            npm run dev > /dev/null 2>&1 &
        else
            npm run dev &
        fi
        frontend_pid=$!
        started_frontend=true
        
        # Wait for frontend to be ready
        print_info "Waiting for frontend to be ready..."
        local attempts=0
        while ! nc -z localhost 1420 2>/dev/null; do
            attempts=$((attempts + 1))
            if [ $attempts -gt 60 ]; then
                print_error "Frontend failed to start within 60 seconds"
                test_failed=true
                break
            fi
            sleep 1
        done
        
        if [ $attempts -le 60 ]; then
            print_success "Frontend is ready on port 1420!"
        fi
    else
        print_success "Frontend already running on port 1420"
    fi
    
    # ============================================================================
    # PHASE 3: Run E2E Tests
    # ============================================================================
    print_header "Phase 3: Running E2E Tests"
    
    cd "$PROJECT_ROOT/client"
    
    # Install Cypress dependencies if needed
    if [ ! -d "node_modules" ]; then
        print_info "Installing client dependencies..."
        if [ "$SILENT" = true ]; then
            npm install --silent 2>/dev/null
        else
            npm install
        fi
    fi
    
    # Test result tracking
    local auth_passed=false
    local security_passed=false
    local e2e_passed=false
    
    # 3a. Auth Tests (Login page tests - always run)
    print_info "Running Authentication Tests..."
    if [ "$SILENT" = true ]; then
        if npx cypress run --spec "cypress/e2e/auth.cy.ts" --headless --quiet 2>/dev/null; then
            auth_passed=true
        else
            test_failed=true
        fi
    else
        if npx cypress run --spec "cypress/e2e/auth.cy.ts" --headless; then
            auth_passed=true
            print_success "✅ Authentication tests passed"
        else
            print_error "❌ Authentication tests failed"
            test_failed=true
        fi
    fi
    
    # 3b. Security Tests (SQL injection, XSS, etc.)
    print_info "Running Security Tests..."
    if [ "$SILENT" = true ]; then
        if npx cypress run --spec "cypress/e2e/security.cy.ts" --headless --quiet 2>/dev/null; then
            security_passed=true
        else
            test_failed=true
        fi
    else
        if npx cypress run --spec "cypress/e2e/security.cy.ts" --headless; then
            security_passed=true
            print_success "✅ Security tests passed"
        else
            print_error "❌ Security tests failed"
            test_failed=true
        fi
    fi
    
    # 3c. Real E2E Tests (Full user journeys - requires backend)
    print_info "Running Real E2E User Journey Tests..."
    if [ "$SILENT" = true ]; then
        if npx cypress run --spec "cypress/e2e/real-e2e/**/*.cy.ts" --headless --quiet 2>/dev/null; then
            e2e_passed=true
        else
            test_failed=true
        fi
    else
        if npx cypress run --spec "cypress/e2e/real-e2e/**/*.cy.ts" --headless; then
            e2e_passed=true
            print_success "✅ Real E2E tests passed"
        else
            print_error "❌ Real E2E tests failed"
            test_failed=true
        fi
    fi
    
    # ============================================================================
    # PHASE 4: Cleanup
    # ============================================================================
    print_header "Phase 4: Cleanup"
    
    if [ "$started_frontend" = true ] && [ -n "$frontend_pid" ]; then
        print_info "Stopping frontend dev server..."
        kill $frontend_pid 2>/dev/null || true
        # Also kill any vite processes
        pkill -f "vite" 2>/dev/null || true
        print_success "Frontend stopped"
    fi
    
    if [ "$started_backend" = true ]; then
        print_info "Stopping backend services..."
        stop_backend
        stop_infrastructure
        print_success "Backend stopped"
    fi
    
    # ============================================================================
    # FINAL REPORT (Always shown, even in silent mode)
    # ============================================================================
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  📊 Test Results Summary${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""
    
    # Show individual test results
    if [ "$auth_passed" = true ]; then
        echo -e "  ${GREEN}✓${NC} Authentication Tests"
    else
        echo -e "  ${RED}✗${NC} Authentication Tests"
    fi
    
    if [ "$security_passed" = true ]; then
        echo -e "  ${GREEN}✓${NC} Security Tests"
    else
        echo -e "  ${RED}✗${NC} Security Tests"
    fi
    
    if [ "$e2e_passed" = true ]; then
        echo -e "  ${GREEN}✓${NC} Real E2E Tests"
    else
        echo -e "  ${RED}✗${NC} Real E2E Tests"
    fi
    
    echo ""
    
    if [ "$test_failed" = true ]; then
        echo -e "${RED}╔════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║              ❌ SOME TESTS FAILED                          ║${NC}"
        echo -e "${RED}╚════════════════════════════════════════════════════════════╝${NC}"
        if [ "$SILENT" = false ]; then
            echo ""
            echo -e "${YELLOW}Run without -s flag to see detailed output.${NC}"
        fi
        exit 1
    else
        echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║              ✅ ALL TESTS PASSED!                          ║${NC}"
        echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo -e "${GREEN}Your application is ready for deployment!${NC}"
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
    if [ "$SILENT" = true ]; then
        docker-compose down -v > /dev/null 2>&1 || docker compose down -v > /dev/null 2>&1 || true
    else
        docker-compose down -v 2>/dev/null || docker compose down -v 2>/dev/null || true
    fi
    
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
    
    if [ "$SILENT" = true ]; then
        echo -e "${GREEN}✓ Full Reset Complete${NC}"
    else
        print_header "✅ Full Reset Complete"
        echo "All services are running with a fresh database."
        echo ""
        echo "  Default admin login:"
        echo "  • Email:    admin@restaurant.com"
        echo "  • Password: admin123"
        echo ""
    fi
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
        -s|--silent)
            SILENT=true
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

if [ "$SILENT" = true ]; then
    echo -e "${GREEN}✓ Startup Complete${NC}"
else
    print_header "🚀 Startup Complete"
    echo "Use './start.sh --stop' to stop all services"
    echo ""
fi

