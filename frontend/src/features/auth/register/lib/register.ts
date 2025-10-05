import { z } from "zod";

export const registerSchema = z.object({
    firstname: z.string().min(3, "first name must be at least 3 characters"),
    lastname: z.string().max(2, "last name must be at least 2 characters"),
    email: z.string().email("Invalid email"),
    confirmPassword: z.string().min(8),
    acceptTerms: z.literal(true, {
        message: "You must accept the terms.",
    }),
    password: z.string().min(8, "Password must be at least 8 characters"),
}).refine((data) => data.password === data.confirmPassword, {
        message: "Passwords do not match",
        path: ["confirmPassword"], // 👈 error will be shown under confirmPassword
    })
export type RegisterFormData = z.infer<typeof registerSchema>;
    