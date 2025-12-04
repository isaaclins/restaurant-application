# 📁 Category Management

> Dynamic category creation, editing, and deletion via the Restaurant Client

---

## Overview

Categories are fully manageable through the REST API. The restaurant can:

- ✅ Create new categories (e.g., "Seasonal Specials", "Kids Menu")
- ✅ Edit category names, descriptions, colors
- ✅ Reorder categories for menu display
- ✅ Deactivate categories (hide from customers without deleting)
- ✅ Delete categories (only if no products are assigned)

---

## Category Entity

```json
{
  "id": 1,
  "name": "Pizza",
  "description": "Fresh Italian pizzas baked in our wood-fired oven",
  "displayOrder": 0,
  "isActive": true,
  "iconUrl": "https://example.com/icons/pizza.svg",
  "colorCode": "#FF5733",
  "createdAt": "2025-12-05T10:00:00",
  "updatedAt": "2025-12-05T10:00:00",
  "productCount": 12
}
```

### Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | ✅ | Category name (2-50 chars, unique) |
| `description` | string | ❌ | Detailed description (max 255 chars) |
| `displayOrder` | integer | ❌ | Order in menu (0 = first) |
| `isActive` | boolean | ❌ | Visibility for customers (default: true) |
| `iconUrl` | string | ❌ | Icon/image URL (max 500 chars) |
| `colorCode` | string | ❌ | Hex color for UI (e.g., #FF5733) |

---

## API Endpoints

### Base URL

```
http://localhost:8080/api/categories
```

### Get All Categories

```http
GET /api/categories
```

Returns all categories ordered by `displayOrder`.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Pizza",
    "displayOrder": 0,
    "isActive": true,
    "productCount": 12
  },
  {
    "id": 2,
    "name": "Pasta",
    "displayOrder": 1,
    "isActive": true,
    "productCount": 8
  }
]
```

### Get Active Categories (Customer-facing)

```http
GET /api/categories/active
```

Returns only active categories for customer menus.

### Get Category by ID

```http
GET /api/categories/{id}
```

### Get Category by Name

```http
GET /api/categories/name/{name}
```

### Create Category

```http
POST /api/categories
Content-Type: application/json

{
  "name": "Seasonal Specials",
  "description": "Limited time offerings",
  "displayOrder": 10,
  "isActive": true,
  "colorCode": "#4CAF50"
}
```

**Response:** `201 Created`

### Update Category

```http
PUT /api/categories/{id}
Content-Type: application/json

{
  "name": "Summer Specials",
  "description": "Cool refreshing dishes for summer",
  "displayOrder": 5
}
```

### Delete Category

```http
DELETE /api/categories/{id}
```

⚠️ **Note:** Only empty categories (no products) can be deleted.

**Error Response (if products exist):**
```json
{
  "status": 400,
  "message": "Cannot delete category with 5 products. Move or delete products first."
}
```

### Toggle Category Active Status

```http
PATCH /api/categories/{id}/toggle-active
```

Quickly enable/disable a category without full update.

### Reorder Categories

```http
PUT /api/categories/reorder
Content-Type: application/json

[3, 1, 2, 5, 4]
```

Provide category IDs in desired order. Display order will be updated automatically.

---

## Integration with Products

Products reference categories by ID:

### Create Product with Category

```http
POST /api/products
Content-Type: application/json

{
  "name": "Margherita",
  "description": "Classic tomato and mozzarella",
  "price": 14.50,
  "categoryId": 1,
  "available": true
}
```

### Filter Products by Category

```http
GET /api/products?categoryId=1
```

---

## Default Categories

On first startup, you can seed default categories. Example SQL:

```sql
INSERT INTO categories (name, description, display_order, is_active) VALUES
('Pizza', 'Fresh Italian pizzas', 0, true),
('Pasta', 'Homemade pasta dishes', 1, true),
('Salads', 'Fresh and healthy salads', 2, true),
('Burgers', 'Gourmet beef burgers', 3, true),
('Desserts', 'Sweet treats', 4, true),
('Beverages', 'Drinks and refreshments', 5, true);
```

Or use the API at startup to create them programmatically.

---

## Restaurant Client Usage

In the Tauri desktop app, the category management screen should allow:

1. **List View** - Drag & drop to reorder
2. **Add Button** - Opens form modal
3. **Edit Icon** - Per row, opens edit modal
4. **Delete Icon** - Per row, with confirmation
5. **Toggle Switch** - Quick active/inactive toggle
6. **Color Picker** - For category color

### Recommended UI Flow

```
Categories List
├── [+] Add Category (button, top right)
├── 🔴 Pizza (12 products) [✏️] [🗑️] [🔘 active]
├── 🟢 Pasta (8 products) [✏️] [🗑️] [🔘 active]
├── 🟠 Salads (5 products) [✏️] [🗑️] [🔘 active]
└── 🔵 Drinks (10 products) [✏️] [🗑️] [○ inactive]
```

---

_Last Updated: 05.12.2025_
