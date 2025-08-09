import { z } from "zod";

export const LoginSchema = z.object({
  email: z.string().email("the email is not valid"),
  password: z.string().min(8, "the minimum length is 8 characters"),
});
export type LoginFormData = z.infer<typeof LoginSchema>;
