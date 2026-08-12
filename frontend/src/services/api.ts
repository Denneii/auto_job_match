// src/services/api.ts
const BASE_URL = 'http://localhost:8080';

export interface VagaPayload {
    titulo: string;
    empresa: string;
    descricaoCompleta: string;
}

export interface VagaAnalisada {
    id: number;
    titulo: string;
    empresa: string;
    descricaoCompleta: string;
    linkVaga?: string;
    porcentagemMatch: number;
    justificativa: string;
    valeApenaAplicar: boolean;
    requisitosAtendidos: string[];
    habilidadesFaltantes: string[];
    curriculoGerado?: string;
    coverLetterGerada?: string;
}

export interface BuscaConfig {
    palavraChave: string;
}

export const api = {
    async login(email: string, senha: string): Promise<{ token: string }> {
        const response = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, senha })
        });
        
        if (!response.ok) throw new Error('Falha no login');
        return response.json();
    },

    async gerarCurriculo(dadosVaga: VagaPayload): Promise<string> {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}/api/vagas/analisar`, { // Ajuste para a sua rota real
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(dadosVaga)
        });

        if (!response.ok) throw new Error('Erro ao gerar currículo');
        return response.text(); 
    },

    listarHistorico: async (): Promise<VagaAnalisada[]> => {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}/api/match`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.status === 401) throw new Error('Sessão expirada');
        if (!response.ok) throw new Error('Erro ao buscar histórico de vagas');

        return response.json();
    },

    iniciarBuscaBot: async (config: BuscaConfig): Promise<void> => {
        const token = localStorage.getItem('token');
        // A rota abaixo depende de como o seu backend está mapeado (ex: /api/bot/buscar)
        const response = await fetch(`${BASE_URL}/api/bot/buscar?palavraChave=${encodeURIComponent(config.palavraChave)}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 401) throw new Error('Sessão expirada');
        if (!response.ok) throw new Error('Erro ao iniciar bot de busca');
    }
};