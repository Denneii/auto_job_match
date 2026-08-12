// src/pages/Login.tsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { api } from "../services/api";
import { useAuth } from "../context/AuthContext";

export function Login() {
    const navigate = useNavigate();
    const { login } = useAuth();

    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [erro, setErro] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        setLoading(true);
        setErro("");

        try {
            const data = await api.login(email, senha);

            console.log("Resposta do login:", data);

            if (!data.token) {
                throw new Error("Token não recebido.");
            }

            // Salva o token no AuthContext
            login(data.token);

            // Vai para o dashboard
            navigate("/dashboard", { replace: true });

        } catch (error) {
            console.error(error);
            setErro("Credenciais inválidas. Verifique seu e-mail e senha.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg border border-gray-100">
                <div>
                    <div className="h-12 w-12 bg-blue-600 rounded-lg mx-auto flex items-center justify-center shadow-sm">
                        <span className="text-white font-bold text-xl">
                            AJM
                        </span>
                    </div>

                    <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
                        Auto Job Match
                    </h2>

                    <p className="mt-2 text-center text-sm text-gray-500">
                        Entre para otimizar seu currículo com IA
                    </p>
                </div>

                <form
                    className="mt-8 space-y-6"
                    onSubmit={handleSubmit}
                >
                    {erro && (
                        <div className="bg-red-50 text-red-500 p-3 rounded-md text-sm text-center border border-red-100">
                            {erro}
                        </div>
                    )}

                    <div className="space-y-4 rounded-md shadow-sm">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                E-mail corporativo
                            </label>

                            <input
                                type="email"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="exemplo@email.com"
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-400 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Senha
                            </label>

                            <input
                                type="password"
                                required
                                value={senha}
                                onChange={(e) => setSenha(e.target.value)}
                                placeholder="••••••••"
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-400 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="group relative w-full flex justify-center py-2.5 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400"
                    >
                        {loading ? "Entrando..." : "Acessar Plataforma"}
                    </button>
                </form>
            </div>
        </div>
    );
}