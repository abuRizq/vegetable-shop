import { createContext, useReducer } from "react";
import { LoginResponse, User } from "../types/auth";


interface AuthState {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoaing: boolean;
    Error: null | string;
}

type AuthAction =
    | { type: "LOGIN" }
    | { type: "LOGIN_SUCCESS"; payload: LoginResponse }
    | { type: 'LOGIN_FAILURE'; payload: string }
    | { type: "LOGUT" }
    | { type: "RESISTER" }
    | { type: "RESISTER_SUCCESS"; payload: LoginResponse }
    | { type: 'RESISTER_FAILURE'; payload: string }
    | { type: "SET_LOADING"; payload: boolean }
    | { type: "SET_LOADING" };

export const initialState: AuthState = {
    user: null,
    token: null,
    isAuthenticated: false,
    isLoaing: false,
    Error: null,
};

export const authReducer = (state: AuthState, actoin: AuthAction): AuthState => {
    switch (actoin.type) {
        case 'LOGIN': return {
            ...state,
            isLoaing: true,
        }
        case 'LOGIN_SUCCESS': return {
            ...state,
            isLoaing: false,
            isAuthenticated: true,
            user: actoin.payload.user,
            token: actoin.payload.token,
        }
        case 'LOGIN_FAILURE': return {
            ...state,
            Error: actoin.payload
        }
        case 'SET_LOADING': {
            return {
                ...state,
                isLoaing: true,
                isAuthenticated: false,
            }
        }
        case 'LOGUT': return {
            ...state,
            isLoaing: false,
            isAuthenticated: false,
            user: null,
            token: null,
        }
        case 'RESISTER':
            return {
                ...state,
                isLoaing: true,
                isAuthenticated: false,
            }
        case 'RESISTER_SUCCESS':
            return {
                ...state,
                isLoaing: false,
                isAuthenticated: true,
                user: actoin.payload.user,
                token: actoin.payload.token,
            }
        case 'RESISTER_FAILURE': return {
            ...state,
            Error: actoin.payload
        }
        case 'SET_LOADING':
            return {
                ...state,
                isLoaing: true,
            }
        default:
            return state;
    }
}

interface AuthContextType extends AuthState {
    login: (email: string, password: string) => Promise<void>;
    logout: () => void;
    clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);