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

start_backend() {
    print_header "Starting Backend Services"
    
    # First start infrastructure
    start_infrastructure
    
    cd "$PROJECT_ROOT/backend"
    
    if [ ! -d "$PROJECT_ROOT/backend" ]; then
        print_warning "Backend directory not found. Skipping..."
        return
    fi
    
    # Start each service in background
    local services=("eureka-server" "api-gateway" "product-service" "cart-service" "order-service" "payment-service" "auth-service")
    
    for service in "${services[@]}"; do
        if [ -d "$PROJECT_ROOT/backend/$service" ]; then
            print_info "Starting $service..."
            cd "$PROJECT_ROOT/backend/$service"
            
            if [ -f "mvnw" ]; then
                ./mvnw spring-boot:run &
            elif [ -f "gradlew" ]; then
                ./gradlew bootRun &
            elif [ -f "pom.xml" ]; then
                mvn spring-boot:run &
            else
                print_warning "$service: No build file found, skipping..."
            fi
        fi
    done
    
    print_success "Backend services starting..."
    echo ""
    echo "  Services:"
    echo "  • Eureka:    http://localhost:8761"
    echo "  • Gateway:   http://localhost:8080"
    echo "  • Products:  http://localhost:8081"
    echo "  • Cart:      http://localhost:8082"
    echo "  • Orders:    http://localhost:8083"
    echo "  • Payments:  http://localhost:8084"
    echo "  • Auth:      http://localhost:8085"
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
        
        local services=("eureka-server" "api-gateway" "product-service" "cart-service" "order-service" "payment-service" "auth-service")
        
        for service in "${services[@]}"; do
            if [ -d "$PROJECT_ROOT/backend/$service" ]; then
                print_info "Testing $service..."
                cd "$PROJECT_ROOT/backend/$service"
                
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
                if npm run test -- --passWithNoTests 2>/dev/null || npm test -- --passWithNoTests 2>/dev/null; then
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
                if npm run test -- --passWithNoTests 2>/dev/null || npm test -- --passWithNoTests 2>/dev/null; then
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

