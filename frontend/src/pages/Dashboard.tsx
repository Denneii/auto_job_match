// src/pages/Dashboard.tsx
import { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { api, type VagaAnalisada } from '../services/api';
import { useAuth } from "../context/AuthContext";

export function Dashboard() {
    const { logout } = useAuth();
    const navigate = useNavigate();

    // Estados do histórico
    const [vagas, setVagas] = useState<VagaAnalisada[]>([]);
    const [carregandoVagas, setCarregandoVagas] = useState(true);
    
    // NOVO: Estado para a busca local nas vagas já analisadas
    const [termoBusca, setTermoBusca] = useState('');

    // Estados da busca automatizada (Bot)
    const [palavraChave, setPalavraChave] = useState('');
    const [buscando, setBuscando] = useState(false);

    useEffect(() => {
        carregarHistorico();
    }, []);

    const carregarHistorico = async () => {
        setCarregandoVagas(true);
        try {
            const data = await api.listarHistorico();
            const vagasOrdenadas = data.sort((a, b) => b.porcentagemMatch - a.porcentagemMatch);
            setVagas(vagasOrdenadas);
        } catch (error) {
            console.error(error);
            alert('Erro ao carregar histórico. Faça login novamente.');
            handleSair();
        } finally {
            setCarregandoVagas(false);
        }
    };

    const handleIniciarBusca = async () => {
        if (!palavraChave.trim()) {
            return alert("Digite o tipo de vaga que deseja buscar (Ex: Desenvolvedor Java Senior)");
        }

        setBuscando(true);
        try {
            await api.iniciarBuscaBot({ palavraChave });
            alert('Busca automatizada concluída!');
            setTermoBusca(''); // Limpa o filtro de busca local ao buscar novas vagas
            await carregarHistorico();
        } catch (error) {
            console.error(error);
            alert('Erro ao iniciar a busca automatizada. Verifique se o backend e o Playwright estão rodando.');
        } finally {
            setBuscando(false);
        }
    };

    const handleSair = () => {
        logout();
        navigate("/login", { replace: true });
    };

    // NOVO: Lógica de filtragem
    const vagasFiltradas = vagas.filter((vaga) => {
        const termo = termoBusca.toLowerCase();
        return (
            vaga.titulo.toLowerCase().includes(termo) ||
            vaga.empresa.toLowerCase().includes(termo) ||
            vaga.justificativa.toLowerCase().includes(termo)
        );
    });

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Navbar */}
            <nav className="bg-white border-b border-gray-200 sticky top-0 z-10">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16">
                        <div className="flex items-center">
                            <div className="flex-shrink-0 flex items-center gap-2">
                                <div className="h-8 w-8 bg-blue-600 rounded-md flex items-center justify-center">
                                    <span className="text-white font-bold text-xs">AJM</span>
                                </div>
                                <span className="font-bold text-xl text-gray-900">AutoJob Match</span>
                            </div>
                        </div>
                        <div className="flex items-center">
                            <button 
                                onClick={handleSair}
                                className="ml-4 px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                Sair
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
                
                {/* Painel de Busca Automatizada */}
                <section className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                    <div className="mb-4">
                        <h2 className="text-lg font-bold text-gray-900">Buscar Novas Vagas (Bot)</h2>
                        <p className="text-sm text-gray-600">Configure o perfil da vaga e deixe a IA procurar e analisar as melhores oportunidades para você.</p>
                    </div>
                    
                    <div className="flex flex-col sm:flex-row gap-4 items-end">
                        <div className="flex-1 w-full">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Tipo de Vaga (Cargo, Tecnologia)
                            </label>
                            <input
                                type="text"
                                value={palavraChave}
                                onChange={(e) => setPalavraChave(e.target.value)}
                                placeholder="Ex: Desenvolvedor Java Especialista"
                                className="w-full p-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                                disabled={buscando}
                            />
                        </div>
                        <button
                            onClick={handleIniciarBusca}
                            disabled={buscando || !palavraChave.trim()}
                            className="w-full sm:w-auto px-6 py-2.5 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:bg-blue-300 transition-colors flex items-center justify-center gap-2"
                        >
                            {buscando ? (
                                <>
                                    <svg className="animate-spin h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Processando Bot...
                                </>
                            ) : (
                                'Iniciar Busca Automatizada'
                            )}
                        </button>
                    </div>
                </section>

                {/* Histórico de Vagas Analisadas */}
                <section>
                    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
                        <h2 className="text-2xl font-bold text-gray-900">Suas Vagas Analisadas</h2>
                        
                        <div className="flex items-center gap-4 w-full sm:w-auto">
                            {/* NOVO: Input de busca local */}
                            <div className="relative flex-1 sm:w-64">
                                <input
                                    type="text"
                                    value={termoBusca}
                                    onChange={(e) => setTermoBusca(e.target.value)}
                                    placeholder="Filtrar vagas..."
                                    className="w-full pl-3 pr-10 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none text-sm"
                                />
                                {termoBusca && (
                                    <button 
                                        onClick={() => setTermoBusca('')}
                                        className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 font-bold px-2"
                                    >
                                        ×
                                    </button>
                                )}
                            </div>

                            <button 
                                onClick={carregarHistorico}
                                disabled={carregandoVagas}
                                className="text-sm text-blue-600 hover:text-blue-800 font-medium disabled:opacity-50 whitespace-nowrap"
                            >
                                Atualizar Lista
                            </button>
                        </div>
                    </div>

                    {carregandoVagas ? (
                        <div className="flex justify-center items-center py-20 text-gray-500">
                            <span className="animate-pulse">Carregando seu histórico de vagas...</span>
                        </div>
                    ) : vagas.length === 0 ? (
                        <div className="bg-white p-12 text-center rounded-xl border border-dashed border-gray-300 text-gray-500">
                            Nenhuma vaga analisada ainda. Utilize o painel acima para buscar.
                        </div>
                    ) : vagasFiltradas.length === 0 ? (
                        // NOVO: Feedback quando a busca não encontra nada
                        <div className="bg-white p-12 text-center rounded-xl border border-dashed border-gray-300 text-gray-500">
                            Nenhuma vaga encontrada para o termo "{termoBusca}".
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                            {/* Modificado para usar vagasFiltradas em vez de vagas */}
                            {vagasFiltradas.map((vaga) => (
                                <div key={vaga.id} className="bg-white flex flex-col rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
                                    <div className="p-5 flex-1">
                                        <div className="flex justify-between items-start mb-3">
                                            <h3 className="text-lg font-bold text-gray-900 line-clamp-2 leading-tight" title={vaga.titulo}>
                                                {vaga.titulo}
                                            </h3>
                                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold whitespace-nowrap ${vaga.porcentagemMatch >= 70 ? 'bg-green-100 text-green-800' : vaga.porcentagemMatch >= 50 ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'}`}>
                                                {vaga.porcentagemMatch}% Match
                                            </span>
                                        </div>
                                        <p className="text-sm font-medium text-gray-600 mb-4">{vaga.empresa}</p>
                                        
                                        <div className="space-y-3">
                                            <div>
                                                <p className="text-xs text-gray-500 uppercase font-semibold mb-1">IA diz:</p>
                                                <p className="text-sm text-gray-700 line-clamp-3" title={vaga.justificativa}>
                                                    {vaga.justificativa}
                                                </p>
                                            </div>

                                            {vaga.valeApenaAplicar && (
                                                <div className="inline-block px-3 py-1 bg-blue-50 text-blue-700 text-xs font-semibold rounded-md border border-blue-100">
                                                    ✨ Recomendado Aplicar
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    
                                    <div className="border-t border-gray-100 bg-gray-50 p-4 flex gap-3">
                                        {vaga.linkVaga ? (
                                            <a 
                                                href={vaga.linkVaga}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="flex-1 text-center px-4 py-2 bg-white border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
                                            >
                                                Ver Vaga Original
                                            </a>
                                        ) : null}
                                        <button className="flex-1 px-4 py-2 bg-gray-800 text-white rounded-md text-sm font-medium hover:bg-gray-900 transition-colors">
                                            Detalhes completos
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </section>

            </main>
        </div>
    );
}