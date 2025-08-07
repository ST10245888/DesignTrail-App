# 🧭 App Navigation Guide

This document provides a complete walkthrough of the navigation flow for different user roles within the **Custom Print & Embroidery Mobile App**. The app is structured with intuitive navigation to help users quickly access the tools and features relevant to their role.

---

## 👥 User Roles

1. **Client** – Customers requesting designs and quotations.
2. **Contractor** – Vendors fulfilling client orders and designs.
3. **Admin** – Platform managers who oversee operations, manage users, and generate reports.

---

## 🔐 Entry Point: Authentication

Upon launching the app, all users go through the following flow:

### 1. **Welcome Screen**
- Options:
  - `Sign Up`
  - `Login`

### 2. **Sign Up / Login**
- After login, the app checks the user’s **role** and navigates them to the appropriate **dashboard**.

---

## 🏠 Dashboards by Role

### 👤 Client Dashboard
After successful login as a **client**, the following tabs or options are available:

| Page | Description |
|------|-------------|
| **Home** | View rotating banners, promotions, and featured products. |
| **Browse Products** | View product listings with filters (category, size, color). |
| **Product Details** | View a specific product, upload artwork, choose customization (print/embroidery), and request a quote. |
| **My Quotations** | Track all past and pending quotes. |
| **My Orders** | View previous orders, reorder, and check order status. |
| **Messages** | Chat with contractors for submitted design requests. |
| **Profile / Settings** | Manage personal details and password. |
| **Logout** | Sign out of the app. |


### 🛠️ Admin Dashboard
After login as an **admin**, navigation includes:

| Page | Description |
|------|-------------|
| **Admin Home** | Overview of platform activity and alerts. |
| **Manage Users** | View, edit, or deactivate users (clients and contractors). |
| **Manage Quotes & Orders** | Monitor quotes submitted, orders processed, and their statuses. |
| **Reports** | Export order statistics, user activity, and revenue data. |
| **Content Management** | Upload or remove banners/flyers on home screen. |
| **Settings** | Admin configuration settings (e.g., payment, roles). |
| **Logout** | Sign out of the app. |


## 🔄 Navigation Flow (General)

    A[Launch App] --> B[Login/Sign Up]
    B --> C{User Role}
    C --> D[Client Dashboard]
    C --> F[Admin Dashboard]
