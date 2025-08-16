//package com.veggieshop.security;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/")
//public class DummyController {
//    // Public
//    @GetMapping("/test/public") public ResponseEntity<?> publicEndpoint() { return ResponseEntity.ok("Public OK"); }
//    @GetMapping("/v3/api-docs") public ResponseEntity<?> docs() { return ResponseEntity.ok("Docs OK"); }
//    @PostMapping("/api/auth/login") public ResponseEntity<?> login() { return ResponseEntity.ok("Login OK"); }
//    @PostMapping("/api/auth/register") public ResponseEntity<?> register() { return ResponseEntity.ok("Register OK"); }
//    @GetMapping("/api/products") public ResponseEntity<?> products() { return ResponseEntity.ok("Products OK"); }
//
//    // Protected
//    @GetMapping("/api/users/me") public ResponseEntity<?> userProfile() { return ResponseEntity.ok("User OK"); }
//    @PostMapping("/api/auth/logout") public ResponseEntity<?> logout() { return ResponseEntity.ok("Logout OK"); }
//    @PostMapping("/api/orders") public ResponseEntity<?> createOrder() { return ResponseEntity.ok("Order OK"); }
//
//    // Admin
//    @PostMapping("/api/products") public ResponseEntity<?> createProduct() { return ResponseEntity.ok("Product Created"); }
//    @PutMapping("/api/categories/{id}") public ResponseEntity<?> updateCategory(@PathVariable Long id) { return ResponseEntity.ok("Category Updated"); }
//    @DeleteMapping("/api/offers/{id}") public ResponseEntity<?> deleteOffer(@PathVariable Long id) { return ResponseEntity.ok("Offer Deleted"); }
//    @PutMapping("/api/users/{id}/role") public ResponseEntity<?> updateUserRole(@PathVariable Long id) { return ResponseEntity.ok("Role Updated"); }
//    @GetMapping("/api/secret-something") public ResponseEntity<?> secret() { return ResponseEntity.ok("Secret OK"); }
//    @GetMapping("/test/protected") public ResponseEntity<?> testProtected() { return ResponseEntity.ok("Protected OK"); }
//}
