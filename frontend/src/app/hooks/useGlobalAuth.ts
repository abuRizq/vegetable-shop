import { useContext } from "react";
import { AuthContextType } from "../types/auth";
import { AuthContext } from "../context/authContext";

export const useGlobalAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useGlobalAuth must be used within an AuthProvider');
    }
    return context;
}; 