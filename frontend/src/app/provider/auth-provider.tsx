"use client ";
import React, { useEffect, useReducer } from 'react'
import { authReducer, initialState } from '../context/authReducer'
import { authService } from '../service/auth.service';
import { LoginResponse } from '../types/auth';
import { AuthContext } from '../context/authContext';

function AuthProvider({ children }: { children: React.ReactNode }) {
    const [state, dispatch] = useReducer(authReducer, initialState)
    useEffect(() => {
        AuthCheck();
    }, []);
    const AuthCheck = async () => {
        const token = localStorage.getItem('auth-token');
        if (token) {
            dispatch({ type: 'LOGIN' })
            try {
                const user = await authService.virfyToken();
                dispatch({ type: 'LOGIN_SUCCESS', payload: user as LoginResponse });
            } catch (error) {
                dispatch({ type: 'LOGIN_FAILURE', payload: 'Session expired' });
            }
        } else {
            dispatch({ type: 'SET_LOADING', payload: false });
        }
    }

    const login = async (email: string, password: string) => {
        dispatch({ type: "LOGIN" })
        try {
            const response = await authService.login({ email, password });
            dispatch({
                type: 'LOGIN_SUCCESS', payload: response
            })
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        } catch (error: any) {
            dispatch({ type: 'LOGIN_FAILURE', payload: error.message })
        }
    }
    const register = async (name: string, email: string, password: string, role: "ADMIN" | "USER") => {
        dispatch({ type: 'RESISTER' })
        try {
            const res = await authService.Register({ name, email, password, role })
            dispatch({ type: 'RESISTER_SUCCESS', payload: res })
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        } catch (error: any) {
            dispatch({ type: 'RESISTER_FAILURE', payload: error.massage })
        }
    }
    const logout = async () => {
        dispatch({ type: 'LOGUT' })
        authService.logout();
        localStorage.removeItem('auth-token');
    }
    const clearError = () => {
        dispatch({ type: 'CLEAR_ERROR' });
    };

    return (
        <AuthContext.Provider value={{
            ...state,
            login,
            logout,
            register,
            clearError,
        }}>
            <div>{children}</div>
        </AuthContext.Provider>
    );
}
export default AuthProvider;