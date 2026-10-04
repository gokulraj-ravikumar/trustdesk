import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import {
    Inbox, Activity, Database, LogOut, AlertCircle, CheckCircle,
    Clock, User, ShoppingCart, Play, FileText, Check, X, ShieldAlert
} from 'lucide-react';

export default function Dashboard() {
    const navigate = useNavigate();

    // Application State
    const [tickets, setTickets] = useState([]);
    const [activeContext, setActiveContext] = useState(null);
    const [pendingActions, setPendingActions] = useState([]); // NEW STATE

    // AI Feature State
    const [triageData, setTriageData] = useState(null);
    const [draftData, setDraftData] = useState(null);
    const [evalData, setEvalData] = useState(null);

    // UI States
    const [loading, setLoading] = useState(false);
    const [triageLoading, setTriageLoading] = useState(false);
    const [draftLoading, setDraftLoading] = useState(false);
    const [evalLoading, setEvalLoading] = useState(false);
    const [showEvalModal, setShowEvalModal] = useState(false);

    useEffect(() => {
        fetchTickets();
    }, []);

    const fetchTickets = async () => {
        try {
            const response = await api.get('/tickets');
            setTickets(response.data);
        } catch (error) {
            console.error("Failed to fetch tickets", error);
        }
    };

    const fetchPendingActions = async (ticketId) => {
        try {
            const response = await api.get(`/tool-actions/ticket/${ticketId}`);
            // Only show actions that still need human approval
            const requiresApproval = response.data.filter(a => a.status === 'APPROVAL_REQUIRED' || a.status === 'REQUESTED');
            setPendingActions(requiresApproval);
        } catch (error) {
            console.error("Failed to fetch pending actions", error);
        }
    };

    const handleLoadSeedData = async () => {
        setLoading(true);
        try {
            await api.post('/data/load');
            await fetchTickets();
        } catch (error) {
            alert("Failed to load seed data.");
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        sessionStorage.removeItem('token');
        navigate('/login');
    };

    // --- AI INTEGRATION HANDLERS ---

    const fetchTicketContext = async (ticketId) => {
        try {
            const response = await api.get(`/tickets/${ticketId}/context`);
            setActiveContext(response.data);
            // Reset AI views when switching tickets
            setTriageData(null);
            setDraftData(null);
            // Fetch any existing pending actions for this ticket
            fetchPendingActions(ticketId);
        } catch (error) {
            console.error("Failed to fetch context", error);
        }
    };

    const runTriage = async () => {
        if (!activeContext) return;
        setTriageLoading(true);
        try {
            const response = await api.post(`/tickets/${activeContext.ticket.id}/triage`);
            setTriageData(response.data);
            fetchTickets();
        } catch (error) {
            alert("Triage execution failed.");
        } finally {
            setTriageLoading(false);
        }
    };

    const generateDraft = async () => {
        if (!activeContext) return;
        setDraftLoading(true);
        try {
            const response = await api.post(`/tickets/${activeContext.ticket.id}/draft-reply`);
            setDraftData(response.data);
            // THE FIX: Immediately fetch the newly generated database rows so we get their real IDs
            fetchPendingActions(activeContext.ticket.id);
        } catch (error) {
            alert("Draft generation failed.");
        } finally {
            setDraftLoading(false);
        }
    };

    const handleToolAction = async (actionId, resolution) => {
        try {
            // Execute the REAL database transaction using the true UUID
            await api.post(`/tool-actions/${actionId}/${resolution}`);
            alert(`Action successfully ${resolution}d!`);
            // Refresh the pending actions list so the approved/rejected action disappears from the UI
            fetchPendingActions(activeContext.ticket.id);
        } catch (error) {
            alert(`Backend failed to ${resolution} the action. Ensure your Spring Boot server is running.`);
        }
    };

    const runEvaluations = async () => {
        setEvalLoading(true);
        setShowEvalModal(true);
        try {
            const response = await api.post('/eval/run');
            setEvalData(response.data);
        } catch (error) {
            alert("Failed to run evaluations.");
            setShowEvalModal(false);
        } finally {
            setEvalLoading(false);
        }
    };

    // --- HELPERS ---

    const priorityColor = (priority) => {
        switch (priority) {
            case 'URGENT': return 'bg-red-100 text-red-800 border-red-200';
            case 'HIGH': return 'bg-orange-100 text-orange-800 border-orange-200';
            case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
            case 'LOW': return 'bg-blue-100 text-blue-800 border-blue-200';
            default: return 'bg-gray-100 text-gray-800 border-gray-200';
        }
    };

    const activeTicket = activeContext?.ticket;
    const customer = activeContext?.customer;
    const order = activeContext?.order;

    return (
        <div className="flex h-screen bg-white font-sans text-gray-800 overflow-hidden">

            {/* EVALUATION MODAL */}
            {showEvalModal && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center">
                    <div className="bg-white rounded-xl shadow-2xl p-8 w-[500px]">
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-2xl font-bold text-gray-900">Eval Runner Results</h2>
                            <button onClick={() => setShowEvalModal(false)} className="text-gray-400 hover:text-gray-600">
                                <X className="w-6 h-6" />
                            </button>
                        </div>
                        {evalLoading ? (
                            <div className="text-center py-8">
                                <Activity className="w-12 h-12 text-blue-500 animate-spin mx-auto mb-4" />
                                <p className="text-gray-600">Running LangChain4j Evaluation Suite...</p>
                            </div>
                        ) : evalData ? (
                            <div className="space-y-4">
                                <div className="flex justify-between p-4 bg-green-50 text-green-800 rounded-lg font-bold text-lg border border-green-200">
                                    <span>Pass Rate:</span>
                                    <span>{evalData.passedCases} / {evalData.totalCases} Passed</span>
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                        <p className="text-xs text-gray-500 font-bold uppercase tracking-wider">Triage Accuracy</p>
                                        <p className="text-xl font-bold text-gray-900">{evalData.triageCategoryAccuracy}%</p>
                                    </div>
                                    <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                        <p className="text-xs text-gray-500 font-bold uppercase tracking-wider">Escalation Accuracy</p>
                                        <p className="text-xl font-bold text-gray-900">{evalData.escalationAccuracy}%</p>
                                    </div>
                                    <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                        <p className="text-xs text-gray-500 font-bold uppercase tracking-wider">Guardrail Block Rate</p>
                                        <p className="text-xl font-bold text-gray-900">{evalData.unsafeActionBlockRate}%</p>
                                    </div>
                                    <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
                                        <p className="text-xs text-gray-500 font-bold uppercase tracking-wider">Citation Coverage</p>
                                        <p className="text-xl font-bold text-gray-900">{evalData.citationCoverageRate}%</p>
                                    </div>
                                </div>
                            </div>
                        ) : null}
                    </div>
                </div>
            )}

            {/* LEFT COLUMN: Navigation Sidebar */}
            <div className="w-64 bg-gray-50 border-r border-gray-200 flex flex-col justify-between">
                <div>
                    <div className="h-16 flex items-center px-6 border-b border-gray-200">
                        <h1 className="text-xl font-bold text-blue-600 tracking-tight flex items-center">
                            <ShieldAlert className="w-6 h-6 mr-2" /> TrustDesk
                        </h1>
                    </div>
                    <nav className="p-4 space-y-1">
                        <button className="w-full flex items-center space-x-3 px-3 py-2 bg-blue-50 text-blue-700 rounded-lg font-medium transition-colors">
                            <Inbox className="w-5 h-5" />
                            <span>Ticket Queue</span>
                        </button>
                        <button
                            onClick={runEvaluations}
                            className="w-full flex items-center space-x-3 px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-lg font-medium transition-colors"
                        >
                            <Activity className="w-5 h-5" />
                            <span>Run Evaluations</span>
                        </button>
                    </nav>
                </div>

                <div className="p-4 border-t border-gray-200 space-y-2">
                    <button
                        onClick={handleLoadSeedData}
                        disabled={loading}
                        className="w-full flex items-center justify-center space-x-2 px-3 py-2 bg-white border border-gray-300 shadow-sm text-gray-700 rounded-lg hover:bg-gray-50 font-medium transition-colors disabled:opacity-50"
                    >
                        <Database className="w-4 h-4" />
                        <span>{loading ? 'Loading...' : 'Load Seed Data'}</span>
                    </button>
                    <button
                        onClick={handleLogout}
                        className="w-full flex items-center justify-center space-x-2 px-3 py-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition-colors"
                    >
                        <LogOut className="w-4 h-4" />
                        <span>Sign Out</span>
                    </button>
                </div>
            </div>

            {/* CENTER COLUMN: Ticket Queue */}
            <div className="w-96 bg-white border-r border-gray-200 flex flex-col h-full">
                <div className="h-16 flex items-center justify-between px-6 border-b border-gray-200 bg-white z-10">
                    <h2 className="text-lg font-semibold text-gray-800">Inbox</h2>
                    <span className="bg-gray-100 text-gray-600 text-xs font-bold px-2 py-1 rounded-full">{tickets.length}</span>
                </div>
                <div className="flex-1 overflow-y-auto">
                    {tickets.length === 0 ? (
                        <div className="p-8 text-center text-gray-500 text-sm">
                            No tickets found. Click "Load Seed Data".
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-100">
                            {tickets.map((t) => (
                                <div
                                    key={t.id}
                                    onClick={() => fetchTicketContext(t.id)}
                                    className={`p-4 cursor-pointer transition-colors ${activeTicket?.id === t.id ? 'bg-blue-50 border-l-4 border-blue-500' : 'hover:bg-gray-50 border-l-4 border-transparent'}`}
                                >
                                    <div className="flex justify-between items-start mb-1">
                                        <span className="font-semibold text-sm text-gray-900">{t.customer?.name || 'Customer'}</span>
                                        <span className="text-xs text-gray-400 font-mono">{t.id}</span>
                                    </div>
                                    <div className="text-sm text-gray-500 truncate mb-2">{t.body}</div>
                                    <div className="flex items-center space-x-2">
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full border ${priorityColor(t.priority)}`}>
                      {t.priority || 'UNRATED'}
                    </span>
                                        <span className="text-xs font-medium text-gray-500 bg-gray-100 px-2 py-0.5 rounded-full">
                      {t.status}
                    </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* RIGHT COLUMN: Active Workspace */}
            <div className="flex-1 bg-gray-50 h-full overflow-y-auto">
                {!activeContext ? (
                    <div className="h-full flex flex-col items-center justify-center text-gray-400 space-y-4">
                        <Inbox className="w-16 h-16 text-gray-300" />
                        <p className="text-lg font-medium text-gray-500">Select a ticket to view workspace</p>
                    </div>
                ) : (
                    <div className="max-w-4xl mx-auto p-8 space-y-6">

                        {/* Header */}
                        <div className="flex justify-between items-start">
                            <div>
                                <h2 className="text-2xl font-bold text-gray-900 mb-1">{customer?.name || 'Customer Request'}</h2>
                                <p className="text-sm text-gray-500 font-mono">{activeTicket.id} • Created {new Date(activeTicket.createdAt).toLocaleDateString()}</p>
                            </div>
                            <span className={`text-sm font-bold px-3 py-1 rounded-full border ${priorityColor(activeTicket.priority)}`}>
                {activeTicket.priority || 'UNRATED'}
              </span>
                        </div>

                        {/* Context Cards */}
                        <div className="grid grid-cols-2 gap-4">
                            <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm flex items-start space-x-3">
                                <div className="bg-blue-100 p-2 rounded-lg text-blue-600"><User className="w-5 h-5"/></div>
                                <div>
                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Customer Profile</p>
                                    <p className="text-sm font-medium text-gray-900">{customer?.email}</p>
                                    <p className="text-xs text-gray-500 mt-1">Tier: <span className="font-semibold">{customer?.tier}</span></p>
                                </div>
                            </div>
                            {order && (
                                <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm flex items-start space-x-3">
                                    <div className="bg-purple-100 p-2 rounded-lg text-purple-600"><ShoppingCart className="w-5 h-5"/></div>
                                    <div>
                                        <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">Order Data</p>
                                        <p className="text-sm font-medium text-gray-900 font-mono">{order.id}</p>
                                        <p className="text-xs text-gray-500 mt-1">Delivered: {new Date(order.deliveredAt).toLocaleDateString()}</p>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Customer Message */}
                        <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                            <div className="bg-gray-50 border-b border-gray-200 px-4 py-3 flex items-center space-x-2">
                                <FileText className="w-4 h-4 text-gray-500" />
                                <span className="font-medium text-gray-700 text-sm">Ticket Body</span>
                            </div>
                            <div className="p-5 text-gray-800 whitespace-pre-wrap leading-relaxed">
                                {activeTicket.body}
                            </div>
                        </div>

                        {/* AI Workspace Grid */}
                        <div className="grid grid-cols-2 gap-6">

                            {/* Left Side: Triage */}
                            <div className="space-y-6">
                                <div className="bg-white rounded-xl border border-indigo-200 shadow-sm overflow-hidden">
                                    <div className="bg-indigo-50 border-b border-indigo-100 px-4 py-3 flex justify-between items-center">
                                        <span className="font-semibold text-indigo-800 text-sm">AI Triage</span>
                                    </div>
                                    <div className="p-5">
                                        {!triageData ? (
                                            <button
                                                onClick={runTriage}
                                                disabled={triageLoading}
                                                className="w-full flex items-center justify-center space-x-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-4 rounded-lg transition-colors disabled:opacity-50"
                                            >
                                                {triageLoading ? <Activity className="w-4 h-4 animate-spin"/> : <Play className="w-4 h-4"/>}
                                                <span>{triageLoading ? 'Analyzing...' : 'Run Triage Engine'}</span>
                                            </button>
                                        ) : (
                                            <div className="space-y-4">
                                                <div className="grid grid-cols-2 gap-4">
                                                    <div className="bg-gray-50 p-3 rounded-lg border border-gray-100">
                                                        <p className="text-xs text-gray-500 mb-1">Category</p>
                                                        <p className="font-semibold text-gray-900">{triageData.category}</p>
                                                    </div>
                                                    <div className="bg-gray-50 p-3 rounded-lg border border-gray-100">
                                                        <p className="text-xs text-gray-500 mb-1">Priority</p>
                                                        <p className="font-semibold text-gray-900">{triageData.priority}</p>
                                                    </div>
                                                </div>
                                                <div className="bg-gray-50 p-3 rounded-lg border border-gray-100">
                                                    <p className="text-xs text-gray-500 mb-1">Escalation Required</p>
                                                    {triageData.shouldEscalate ? (
                                                        <span className="flex items-center text-red-600 font-semibold text-sm"><AlertCircle className="w-4 h-4 mr-1"/> Yes - Flagged</span>
                                                    ) : (
                                                        <span className="flex items-center text-green-600 font-semibold text-sm"><CheckCircle className="w-4 h-4 mr-1"/> No</span>
                                                    )}
                                                </div>
                                                <p className="text-sm text-gray-600 italic">"{triageData.reasonSummary}"</p>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Right Side: Draft & Actions */}
                            <div className="space-y-6">
                                <div className="bg-white rounded-xl border border-blue-200 shadow-sm overflow-hidden">
                                    <div className="bg-blue-50 border-b border-blue-100 px-4 py-3 flex justify-between items-center">
                                        <span className="font-semibold text-blue-800 text-sm">AI Draft Reply (RAG)</span>
                                    </div>
                                    <div className="p-5">
                                        {!draftData ? (
                                            <button
                                                onClick={generateDraft}
                                                disabled={draftLoading}
                                                className="w-full flex items-center justify-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors disabled:opacity-50"
                                            >
                                                {draftLoading ? <Activity className="w-4 h-4 animate-spin"/> : <FileText className="w-4 h-4"/>}
                                                <span>{draftLoading ? 'Generating...' : 'Generate Draft'}</span>
                                            </button>
                                        ) : (
                                            <div className="space-y-4">
                                                <div className="p-4 bg-gray-50 rounded-lg border border-gray-200 text-sm text-gray-800 whitespace-pre-wrap leading-relaxed">
                                                    {draftData.draftBody}
                                                </div>
                                                {draftData.citations && draftData.citations.length > 0 && (
                                                    <div className="flex flex-wrap gap-2">
                                                        {draftData.citations.map(cite => (
                                                            <span key={cite} className="bg-blue-100 text-blue-800 text-xs font-bold px-2 py-1 rounded-full border border-blue-200">
                                {cite}
                              </span>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        )}
                                    </div>
                                </div>

                                {/* THE FIX: We map over the real DB pendingActions instead of the raw Draft recommendation */}
                                {pendingActions.length > 0 && (
                                    <div className="bg-white rounded-xl border border-amber-200 shadow-sm overflow-hidden">
                                        <div className="bg-amber-50 border-b border-amber-200 px-4 py-3 flex items-center space-x-2">
                                            <AlertCircle className="w-4 h-4 text-amber-600"/>
                                            <span className="font-semibold text-amber-800 text-sm">Approval Required</span>
                                        </div>
                                        <div className="p-4 space-y-3">
                                            {pendingActions.map(action => (
                                                <div key={action.id} className="bg-gray-50 p-3 rounded-lg border border-gray-200">
                                                    <p className="text-sm font-mono text-gray-800 mb-1">{action.toolName}</p>
                                                    <p className="text-xs text-gray-500 mb-3 font-mono">ID: {action.id}</p>
                                                    <div className="flex space-x-2">
                                                        <button
                                                            onClick={() => handleToolAction(action.id, 'approve')}
                                                            className="flex-1 bg-green-600 hover:bg-green-700 text-white text-xs font-bold py-2 px-3 rounded flex items-center justify-center transition-colors"
                                                        >
                                                            <Check className="w-3 h-3 mr-1"/> Approve
                                                        </button>
                                                        <button
                                                            onClick={() => handleToolAction(action.id, 'reject')}
                                                            className="flex-1 bg-red-600 hover:bg-red-700 text-white text-xs font-bold py-2 px-3 rounded flex items-center justify-center transition-colors"
                                                        >
                                                            <X className="w-3 h-3 mr-1"/> Reject
                                                        </button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>

                        </div>
                    </div>
                )}
            </div>

        </div>
    );
}