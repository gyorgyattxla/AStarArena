import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { Play, CheckCircle2, AlertCircle, Loader2, ArrowLeft } from 'lucide-react';
import { heuristicService } from '../services/api';

const DEFAULT_CODE = `public int heur(SokobanState state) {
    // Write your custom A* heuristic here.
    // The state provides: getPlayer(), getBoxes(), getTargets(), getWalls()
    // Return the estimated distance to a winning state.
    
    int score = 0;
    
    // TODO: Implement your logic
    
    return score;
}`;

export default function Submit() {
    const [code, setCode] = useState(DEFAULT_CODE);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submissionResult, setSubmissionResult] = useState(null);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async () => {
        if (!code.trim()) {
            setError("Source code cannot be empty.");
            return;
        }

        setIsSubmitting(true);
        setError('');
        setSubmissionResult(null);

        try {
            const response = await heuristicService.submit(code);
            setSubmissionResult(response);
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to submit heuristic. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-6xl mx-auto space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">The Arena</h1>
                    <p className="text-gray-500 mt-1">Implement the <code className="bg-gray-100 px-1.5 py-0.5 rounded text-sm text-blue-600">heur(SokobanState state)</code> method.</p>
                </div>
                <button
                    onClick={() => navigate('/')}
                    className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors font-medium"
                >
                    <ArrowLeft size={18} /> Back to Dashboard
                </button>
            </div>

            {/* Error Banner */}
            {error && (
                <div className="p-4 bg-red-50 border-l-4 border-red-500 rounded-r-md flex items-start gap-3 shadow-sm">
                    <AlertCircle className="text-red-500 shrink-0 mt-0.5" size={18} />
                    <p className="text-sm text-red-700 font-medium">{error}</p>
                </div>
            )}

            {/* Success Banner */}
            {submissionResult && (
                <div className="p-4 bg-green-50 border-l-4 border-green-500 rounded-r-md flex flex-col gap-2 shadow-sm">
                    <div className="flex items-start gap-3">
                        <CheckCircle2 className="text-green-600 shrink-0 mt-0.5" size={20} />
                        <div>
                            <h3 className="text-green-800 font-bold">Submission Accepted!</h3>
                            <p className="text-sm text-green-700 mt-1">
                                Your heuristic has been sent to the judging queue (ID: {submissionResult.id}). 
                                The Docker workers are processing it now.
                            </p>
                        </div>
                    </div>
                    <div className="ml-8 mt-2">
                        <button 
                            onClick={() => navigate('/leaderboard')}
                            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
                        >
                            Go to Leaderboard
                        </button>
                    </div>
                </div>
            )}

            {/* Editor Workspace */}
            <div className="bg-gray-900 rounded-2xl shadow-lg overflow-hidden border border-gray-800 flex flex-col">
                <div className="flex items-center justify-between px-4 py-3 bg-gray-950 border-b border-gray-800">
                    <div className="flex items-center gap-2">
                        <div className="flex gap-1.5">
                            <div className="w-3 h-3 rounded-full bg-red-500"></div>
                            <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                            <div className="w-3 h-3 rounded-full bg-green-500"></div>
                        </div>
                        <span className="ml-3 text-sm font-mono text-gray-400">UserHeuristic.java</span>
                    </div>
                    <button
                        onClick={handleSubmit}
                        disabled={isSubmitting}
                        className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white px-4 py-1.5 rounded-md text-sm font-bold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isSubmitting ? (
                            <><Loader2 size={16} className="animate-spin" /> Compiling...</>
                        ) : (
                            <><Play size={16} fill="currentColor" /> Submit Code</>
                        )}
                    </button>
                </div>
                
                <div className="h-[500px] w-full relative">
                    <Editor
                        height="100%"
                        defaultLanguage="java"
                        theme="vs-dark"
                        value={code}
                        onChange={(value) => setCode(value || '')}
                        options={{
                            minimap: { enabled: false },
                            fontSize: 14,
                            wordWrap: "on",
                            padding: { top: 16, bottom: 16 },
                            scrollBeyondLastLine: false,
                            smoothScrolling: true,
                        }}
                    />
                </div>
            </div>
        </div>
    );
}