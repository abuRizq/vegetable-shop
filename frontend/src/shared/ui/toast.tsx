"use client";

import { toast, ToastOptions, TypeOptions } from "react-toastify";

export type ToastType = TypeOptions;

export interface ToastProps {
  message: string;
  type?: ToastType;
  options?: ToastOptions;
}

export const showToast = ({ message, type = "default", options }: ToastProps) => {
  const defaultOptions: ToastOptions = {
    position: "top-right",
    autoClose: 3000,
    hideProgressBar: false,
    closeOnClick: true,
    pauseOnHover: true,
    draggable: true,
    ...options,
  };

  switch (type) {
    case "success":
      return toast.success(message, defaultOptions);
    case "error":
      return toast.error(message, defaultOptions);
    case "warning":
      return toast.warning(message, defaultOptions);
    case "info":
      return toast.info(message, defaultOptions);
    default:
      return toast(message, defaultOptions);
  }
};

export const Toast = {
  success: (message: string, options?: ToastOptions) =>
    showToast({ message, type: "success", options }),
  error: (message: string, options?: ToastOptions) =>
    showToast({ message, type: "error", options }),
  warning: (message: string, options?: ToastOptions) =>
    showToast({ message, type: "warning", options }),
  info: (message: string, options?: ToastOptions) =>
    showToast({ message, type: "info", options }),
  default: (message: string, options?: ToastOptions) =>
    showToast({ message, type: "default", options }),
};
