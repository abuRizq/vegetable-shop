"use client ";
import React, { useReducer } from 'react'
import { authReducer, initialState } from '../context/authContext'
import { authService } from '../service/auth.service';
import { LoginResponse } from '../types/auth';
import { Login } from '@mui/icons-material';

function AuthProvider({ children }: { children: React.ReactNode }) {
    const [state, dispatch] = useReducer(authReducer, initialState)

    const token = localStorage.getItem('auth-token');
    const AuthCheck = async () => {
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

    const login = async (eamil: string, password: string) => {
        dispatch({ type: "LOGIN" })
        try {
            const response = await authService.login(eamil, password);
            dispatch({
                type: 'LOGIN_SUCCESS', payload: response as LoginResponse
            })
        } catch (error) {

        }

        return (
            <div>{children}</div>
        )
    }

    export default AuthProvider