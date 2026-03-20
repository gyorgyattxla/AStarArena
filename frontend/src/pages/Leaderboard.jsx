import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { heuristicService } from '../services/api';
import { Trophy, Clock, Cpu, AlertCircle, Loader2 } from 'lucide-react';

export default function Leaderboard() {
    const { data: submissions, isLoading, error } = useQuery({
        queryKey: ['leaderboard'],
        queryFn: heuristicService.getLeaderboard,
    });

    if (isLoading) {
        return (
            <div className="flex flex-col items-center justify-center h-64 text-gray-500">
                <Loader2 size={40} className="animate-spin mb-4 text-blue-600" />
                <p className="font-medium">Loading rankings...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-4 bg-red-50 border-l-4 border-red-500 rounded-r-md flex items-start gap-3 shadow-sm max-w-4xl mx-auto">
                <AlertCircle className="text-red-500 shrink-0 mt-0.5" size={18} />
                <p className="text-sm text-red-700 font-medium">Failed to load leaderboard data.</p>
            </div>
        );
    }

    return (
        <div className="max-w-5xl mx-auto space-y-6">
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200 flex items-center gap-4">
                <div className="bg-purple-100 p-3 rounded-lg">
                    <Trophy className="text-purple-600" size={28} />
                </div>
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Global Leaderboard</h1>
                    <p className="text-gray-500 mt-1">Ranked by lowest nodes expanded, followed by execution time.</p>
                </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-gray-50 border-b border-gray-200 text-gray-600 text-sm uppercase tracking-wider">
                                <th className="px-6 py-4 font-semibold">Rank</th>
                                <th className="px-6 py-4 font-semibold">Engineer</th>
                                <th className="px-6 py-4 font-semibold">
                                    <div className="flex items-center gap-2"><Cpu size={16}/> Nodes Expanded</div>
                                </th>
                                <th className="px-6 py-4 font-semibold">
                                    <div className="flex items-center gap-2"><Clock size={16}/> Time (ms)</div>
                                </th>
                                <th className="px-6 py-4 font-semibold">Date</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {submissions?.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="px-6 py-8 text-center text-gray-500">
                                        No successful heuristics submitted yet. Be the first!
                                    </td>
                                </tr>
                            ) : (
                                submissions?.map((sub, index) => (
                                    <tr key={sub.id} className="hover:bg-gray-50 transition-colors">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className={`inline-flex items-center justify-center w-8 h-8 rounded-full font-bold ${
                                                index === 0 ? 'bg-yellow-100 text-yellow-700' : 
                                                index === 1 ? 'bg-gray-200 text-gray-700' : 
                                                index === 2 ? 'bg-orange-100 text-orange-700' : 
                                                'bg-blue-50 text-blue-700'
                                            }`}>
                                                {index + 1}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">
                                            {sub.author?.displayedName || 'Anonymous'}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap font-mono text-blue-600">
                                            {sub.nodesExpanded?.toLocaleString() || '-'}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-gray-600">
                                            {sub.executionTimeMs || '-'} ms
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(sub.submittedAt).toLocaleDateString()}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}