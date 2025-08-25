
declare namespace Orders {
    export interface Order {
        id: number;
        user_id: number;
        total_price: number;
        status: string;
        created_at: string;
        updated_at: string;
        orderItems?: OrderItem[];
    }
    export interface OrderItem {
        id: number;
        order_id: number;
        product_id: number;
        quantity: number;
        price: number;
        created_at: string;
        updated_at: string;
        product: Product;
    };
}

declare namespace Poudects {
    export interface Category {
        id: number;
        name: string;
        description: string;
        image_url?: string;
        created_at?: string;
        updated_at?: string;
    }
    export interface proudect {
        id: number;
        name: string;
        description: string;
        price: number;
        discount: number;
        image_url: string;
        category_id: string;
        sold_count: number;
        is_featured: boolean;
        created_at: string;
        updated_at: string;
    }
    export interface offer {
        id: number;
        product_id: number;
        description: string;
        discount: number;
        start_date: string;
        end_date: string;
        product?: Product[];
    }
}
export type Order = Orders.Order;
export type OrderItem = Orders.OrderItem;
export type Product = Products.Product;
export type Category = Products.Category;
export type Offer = Products.Offer;
// API Response types (useful for frontend)
export interface ApiResponse<T> {
    data: T;
    message: string;
    success: boolean;
}

export interface PaginatedResponse<T> {
    data: T[];
    total: number;
    page: number;
    limit: number;
    totalPages: number;
}
export type UserResponse = ApiResponse<User>;
export type UsersResponse = ApiResponse<PaginatedResponse<User>>;
export type OrderResponse = ApiResponse<Order>;
export type OrdersResponse = ApiResponse<PaginatedResponse<Order>>;
export type ProductResponse = ApiResponse<Product>;
export type ProductsResponse = ApiResponse<PaginatedResponse<Product>>;
export type CategoryResponse = ApiResponse<Category>;
export type CategoriesResponse = ApiResponse<Category[]>;