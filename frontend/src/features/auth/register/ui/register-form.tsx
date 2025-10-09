"use client";

import type React from "react";
import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  Eye,
  EyeOff,
  Mail,
  Loader2,
  Leaf,
  User,
  Check,
  AlertCircle,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  RegisterFormData,
  registerSchema,
} from "@/features/auth/register/lib/register";
import { useRegisterMutation } from "../api/use-register";
import { Toast } from "@/shared/ui/toast";

export const RegisterForm = () => {
  const { mutate: Register } = useRegisterMutation({
    onSuccess: (data, Variables) => {
      console.log(data);
      console.log(Variables);
      router.push("/");
    },
    onError: (error, Variables) => {
      Toast.error(error.message);
      console.log(Variables);
    },
  });
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [subscribeNewsletter, setSubscribeNewsletter] = useState(true);
  const [registerError, setRegisterError] = useState({ massage: "" });

  const router = useRouter();

  const onSubmit = async (data: RegisterFormData) => {
    try {
      Register({
        name: data.firstname,
        email: data.email,
        password: data.password,
      });
    } catch (error) {
      setRegisterError({ massage: "Registration failed. Please try again." });
    }
  };

  return (
    <div
      className="min-h-screen w-full flex items-center justify-center relative overflow-hidden py-8"
      style={{ backgroundColor: "hsl(var(--background))" }}
    >
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-5">
        <div className="absolute inset-0 bg-[url('data:image/svg+xml,%3Csvg width=60 height=60 viewBox=0 0 60 60 xmlns=http://www.w3.org/2000/svg%3E%3Cg fill=none fillRule=evenodd%3E%3Cg fill=%23000000 fillOpacity=0.1%3E%3Ccircle cx=7 cy=7 r=1/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')]"></div>
      </div>

      {/* Floating Vegetable Icons */}
      <div className="absolute inset-0 pointer-events-none">
        {/* Top decorations */}
        <div className="absolute top-16 left-16 w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center animate-bounce">
          <span className="text-2xl">🥬</span>
        </div>
        <div className="absolute top-24 right-20 w-12 h-12 bg-secondary/10 rounded-full flex items-center justify-center animate-pulse">
          <span className="text-xl">🥕</span>
        </div>
        <div className="absolute top-32 left-1/4 w-10 h-10 bg-accent/10 rounded-full flex items-center justify-center animate-bounce delay-300">
          <span className="text-lg">🥒</span>
        </div>

        {/* Bottom decorations */}
        <div className="absolute bottom-20 left-20 w-14 h-14 bg-accent/10 rounded-full flex items-center justify-center animate-bounce delay-500">
          <span className="text-xl">🍅</span>
        </div>
        <div className="absolute bottom-32 right-16 w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center animate-pulse delay-700">
          <span className="text-xl">🫑</span>
        </div>
        <div className="absolute bottom-16 right-1/4 w-8 h-8 bg-secondary/10 rounded-full flex items-center justify-center animate-bounce delay-1000">
          <span className="text-sm">🥦</span>
        </div>

        {/* Center decorations */}
        <div className="absolute top-1/3 left-1/6 w-6 h-6 bg-primary/5 rounded-full animate-ping"></div>
        <div className="absolute top-2/3 right-1/6 w-4 h-4 bg-secondary/10 rounded-full animate-pulse delay-200"></div>
        <div className="absolute top-1/2 left-1/12 w-8 h-8 bg-accent/5 rounded-full animate-bounce delay-800"></div>
      </div>

      {/* Register Form Container */}
      <div className="relative z-10 w-full max-w-lg mx-4">
        <div
          className="card-elevated p-8 rounded-2xl shadow-2xl backdrop-blur-xl border"
          style={{
            backgroundColor: "hsl(var(--paper) / 0.95)",
            borderColor: "hsl(var(--divider))",
            boxShadow: "var(--shadow-lg)",
          }}
        >
          {/* Logo and Branding */}
          <div className="text-center mb-8">
            <div className="flex items-center justify-center mb-6">
              <div className="w-16 h-16 bg-primary rounded-2xl flex items-center justify-center shadow-green mr-4">
                <Leaf className="w-8 h-8 text-white animate-leaf-sway" />
              </div>
              <div>
                <h1
                  className="text-2xl font-bold"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  FreshVeggies
                </h1>
                <p
                  className="text-sm"
                  style={{ color: "hsl(var(--text-secondary))" }}
                >
                  Farm Fresh Delivery
                </p>
              </div>
            </div>

            <h2
              className="text-2xl font-bold mb-2"
              style={{ color: "hsl(var(--text-primary))" }}
            >
              Join FreshVeggies
            </h2>
            <p style={{ color: "hsl(var(--text-secondary))" }}>
              Create your account and start enjoying fresh, organic vegetables
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            {/* Submit Error Message */}
            {registerError.massage && (
              <div
                className="flex items-center space-x-3 p-4 rounded-lg border"
                style={{
                  backgroundColor: "hsl(var(--error) / 0.1)",
                  borderColor: "hsl(var(--error) / 0.3)",
                  color: "hsl(var(--error))",
                }}
                role="alert"
              >
                <AlertCircle className="w-5 h-5 flex-shrink-0" />
                <span className="text-sm">{registerError.massage}</span>
              </div>
            )}
            {/* Name Fields */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label
                  htmlFor="firstName"
                  className="block text-sm font-medium"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  First Name
                </label>
                <div className="relative">
                  <User
                    className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5"
                    style={{ color: "hsl(var(--text-disabled))" }}
                  />
                  <input
                    id="firstname"
                    {...register("firstname")}
                    name="firstname"
                    type="text"
                    required
                    disabled={isSubmitting}
                    placeholder="Enter first name"
                    className={`input w-full pl-10 pr-4 py-3 rounded-lg focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed`}
                  />
                </div>
                {errors.firstname && (
                  <p className="text-sm" style={{ color: "hsl(var(--error))" }}>
                    {errors.firstname.message}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <label
                  htmlFor="lastName"
                  className="block text-sm font-medium"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  Last Name
                </label>
                <div className="relative">
                  <User
                    className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5"
                    style={{ color: "hsl(var(--text-disabled))" }}
                  />
                  <input
                    id="lastName"
                    type="text"
                    {...register("lastname")}
                    name="lastName"
                    required
                    disabled={isSubmitting}
                    placeholder="Enter last name"
                    className={`input w-full pl-10 pr-4 py-3 rounded-lg focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed `}
                  />
                </div>
                {errors.lastname && (
                  <p className="text-sm" style={{ color: "hsl(var(--error))" }}>
                    {errors.lastname.message}
                  </p>
                )}
              </div>
            </div>

            {/* Email Field */}
            <div className="space-y-2">
              <label
                htmlFor="email"
                className="block text-sm font-medium"
                style={{ color: "hsl(var(--text-primary))" }}
              >
                Email Address
              </label>
              <div className="relative">
                <Mail
                  className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5"
                  style={{ color: "hsl(var(--text-disabled))" }}
                />
                <input
                  id="email"
                  // name="email"
                  type="email"
                  {...register("email")}
                  required
                  disabled={isSubmitting}
                  placeholder="Enter your email"
                  className={`input w-full pl-10 pr-4 py-3 rounded-lg focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed`}
                />
              </div>
              {errors.email && (
                <p className="text-sm" style={{ color: "hsl(var(--error))" }}>
                  {errors.email.message}
                </p>
              )}
            </div>
            {/* Password Fields */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label
                  htmlFor="password"
                  className="block text-sm font-medium"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  Password
                </label>
                <div className="relative">
                  <input
                    id="password"
                    // name="password"
                    type={showPassword ? "text" : "password"}
                    {...register("password")}
                    required
                    disabled={isSubmitting}
                    placeholder="Create password"
                    className={`input w-full px-4 py-3 pr-12 rounded-lg focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed `}
                  />
                  {errors.password && (
                    <p
                      className="text-sm"
                      style={{ color: "hsl(var(--error))" }}
                    >
                      {errors.password.message}
                    </p>
                  )}
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    disabled={isSubmitting}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 transition-colors duration-200 disabled:opacity-50"
                    style={{ color: "hsl(var(--text-disabled))" }}
                  >
                    {showPassword ? (
                      <EyeOff className="w-5 h-5" />
                    ) : (
                      <Eye className="w-5 h-5" />
                    )}
                  </button>
                </div>
                {/* {errors.password && (
                                    <p className="text-sm" style={{ color: "hsl(var(--error))" }}>
                                        {errors.password}
                                    </p>
                                )} */}
              </div>

              <div className="space-y-2">
                <label
                  htmlFor="confirmPassword"
                  className="block text-sm font-medium"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  Confirm Password
                </label>
                <div className="relative">
                  <input
                    id="confirmPassword"
                    // name="confirmPassword"
                    type={showConfirmPassword ? "text" : "password"}
                    {...register("confirmPassword")}
                    required
                    disabled={isSubmitting}
                    placeholder="Confirm password"
                    className={`input w-full px-4 py-3 pr-12 rounded-lg focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed `}
                  />
                  {errors.confirmPassword && (
                    <p
                      className="text-sm"
                      style={{ color: "hsl(var(--error))" }}
                    >
                      {errors.confirmPassword.message}
                    </p>
                  )}

                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    disabled={isSubmitting}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 transition-colors duration-200 disabled:opacity-50"
                    style={{ color: "hsl(var(--text-disabled))" }}
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="w-5 h-5" />
                    ) : (
                      <Eye className="w-5 h-5" />
                    )}
                  </button>
                </div>
                {/* {errors.confirmPassword && (
                                    <p className="text-sm" style={{ color: "hsl(var(--error))" }}>
                                        {errors.confirmPassword}
                                    </p>
                                )} */}
              </div>
            </div>

            {/* Checkboxes */}
            <div className="space-y-3">
              <label className="flex items-start space-x-3">
                <input
                  type="checkbox"
                  {...register("acceptTerms")}
                  className="w-4 h-4 mt-0.5 rounded border-2 focus:ring-2 focus:ring-offset-0"
                  style={{
                    accentColor: "hsl(var(--primary))",
                    borderColor: "hsl(var(--divider))",
                  }}
                />
                <span
                  className="text-sm"
                  style={{ color: "hsl(var(--text-secondary))" }}
                >
                  I agree to the{" "}
                  <button
                    type="button"
                    className="font-medium transition-colors duration-200 hover:underline"
                    style={{ color: "hsl(var(--primary))" }}
                  >
                    Terms of Service
                  </button>{" "}
                  and{" "}
                  <button
                    type="button"
                    className="font-medium transition-colors duration-200 hover:underline"
                    style={{ color: "hsl(var(--primary))" }}
                  >
                    Privacy Policy
                  </button>
                </span>
              </label>
              {/* {errors.terms && (
                                <p className="text-sm" style={{ color: "hsl(var(--error))" }}>
                                    {errors.terms}
                                </p>
                            )} */}

              <label className="flex items-start space-x-3">
                <input
                  type="checkbox"
                  checked={subscribeNewsletter}
                  onChange={(e) => setSubscribeNewsletter(e.target.checked)}
                  className="w-4 h-4 mt-0.5 rounded border-2 focus:ring-2 focus:ring-offset-0"
                  style={{
                    accentColor: "hsl(var(--primary))",
                    borderColor: "hsl(var(--divider))",
                  }}
                />
                <span
                  className="text-sm"
                  style={{ color: "hsl(var(--text-secondary))" }}
                >
                  Subscribe to our newsletter for fresh deals and seasonal
                  offers
                </span>
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary w-full py-3 px-4 rounded-lg font-semibold transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none flex items-center justify-center space-x-2"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  <span>Creating Account...</span>
                </>
              ) : (
                <>
                  <Check className="w-5 h-5" />
                  <span>Create Account</span>
                </>
              )}
            </button>

            {/* Google Sign Up */}
            <button
              type="button"
              className="w-full flex items-center justify-center space-x-3 py-3 px-4 rounded-lg font-medium transition-all duration-200 card hover:shadow-md"
            >
              <svg className="w-5 h-5" viewBox="0 0 24 24">
                <path
                  fill="currentColor"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="currentColor"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="currentColor"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                />
                <path
                  fill="currentColor"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                />
              </svg>
              <span style={{ color: "hsl(var(--text-primary))" }}>
                Sign up with Google
              </span>
            </button>
          </form>

          {/* Sign In Link */}
          <div className="mt-8 text-center">
            <p
              className="text-sm"
              style={{ color: "hsl(var(--text-secondary))" }}
            >
              Already have an account?{" "}
              <button
                type="button"
                onClick={() => router.push("/login")}
                className="font-medium transition-colors duration-200 hover:underline"
                style={{ color: "hsl(var(--primary))" }}
              >
                Sign in
              </button>
            </p>
          </div>

          {/* Bottom tagline */}
          <div className="mt-6 text-center">
            <p
              className="text-xs"
              style={{ color: "hsl(var(--text-disabled))" }}
            >
              🌱 Join thousands of happy customers • 🥬 Fresh produce delivered
              daily
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
