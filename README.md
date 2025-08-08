
 # 📱 Custom Print & Embroidery Mobile App

This mobile application allows clients to request custom printing and embroidery services by submitting artwork, browsing products, generating quotations, and communicating with contractors. Contractors and administrators can manage orders, communicate with clients, and ensure smooth operations on the platform.

## Features

### 🔐 User Authentication & Access
- **User Registration & Login**: Sign-up, login, logout, and password reset capabilities. Users can securely access the system using their email and password.
- **Role-Based Access Control (RBAC)**: Different access levels for:
  - **Clients**: View products, submit design requests, communicate, and pay.
  - **Contractors**: View submitted designs, respond with pricing, and communicate.
  - **Admins**: Manage users, orders, content, and platform data.

### 🛍️ Product Browsing & Customization
- **Product Listing Page**: Products are displayed with filtering options (category, size, color).
- **Product Details Page**: Each product includes images, descriptions, and customization options.
- **Customization Feature**: Upload design artwork and choose between printing or embroidery.

### 📄 Quotations & Orders
- **Generate Quotation**: Dynamically calculate and generate downloadable quote PDFs based on selections.
- **View Past Orders**: Clients can review, reorder, or consult past design requests, pricing, and statuses.

### 💬 Communication & Messaging
- **Client-Contractor Messaging**: Secure real-time or asynchronous messaging system to discuss order details.

### 💳 Secure Payments
- **Payment Gateway Integration**: Yoco and PayFast integration for secure payment processing. 
- **Order Confirmation & Receipts**: Clients receive confirmation notifications and downloadable receipts upon successful payment.

### 🛠️ Admin Features
- **Admin Dashboard**: Admins can:
  - Manage quotes and orders
  - Approve or reject user-uploaded artwork
  - View/export activity reports
  - Manage platform users and content

### 🖼️ Home Page
- **Banners & Flyers**: Home screen with rotating promotional banners and flyers fetched from the backend

### 📊 Reporting
- **Export Reports**: Admins can generate and export reports on user activity, orders, payments, and more to support data-driven decision-making.

## 🧱 Tech Stack

| Layer         | Technology             |
|---------------|------------------------|
| Frontend      | Flutter / Kotlin / React Native (based on actual tech used) |
| Backend       | Node.js / Kotlin / Firebase (based on actual backend) |
| Authentication| Firebase Auth |
| Database      | Firebase Firestore / PostgreSQL / MongoDB |
| Cloud Storage | Firebase Storage / AWS S3 |
| Messaging     | Ably, Firebase Realtime DB |
| PDF Generation| HTML2PDF / jsPDF / Custom |
| Reports       | CSV/XLS Export, Charts |

