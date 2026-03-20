import React, { useState } from 'react';
import { mapService } from '../services/api';
import { Map, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';

export default function MapUpload() {
    const [content, setContent] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess(false);
        setIsSubmitting(true);

        try {
            await mapService.uploadMap(content);
            setSuccess(true);
            setContent('');
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to upload map. Check formatting rules.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-200">
                <div className="flex items-center gap-3 mb-4">
                    <Map className="text-green-600" size={28} />
                    <h1 className="text-2xl font-bold text-gray-900">Upload Sokoban Map</h1>
                </div>
                <p className="text-gray-600 mb-6">
                    Paste your ASCII map layout below. 
                    Valid characters: <code>#</code> (Wall), <code>@</code> (Player), <code>$</code> (Box), <code>.</code> (Target), <code>*</code> (Box on Target), <code>+</code> (Player on Target).
                </p>

                {error && (
                    <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 rounded-r-md flex items-start gap-3">
                        <AlertCircle className="text-red-500 shrink-0 mt-0.5" size={18} />
                        <p className="text-sm text-red-700 font-medium">{error}</p>
                    </div>
                )}

                {success && (
                    <div className="mb-6 p-4 bg-green-50 border-l-4 border-green-500 rounded-r-md flex items-start gap-3">
                        <CheckCircle2 className="text-green-500 shrink-0 mt-0.5" size={18} />
                        <p className="text-sm text-green-700 font-medium">Map successfully validated and added to the Arena!</p>
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        required
                        rows={10}
                        className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 font-mono text-sm resize-y"
                        placeholder="#######&#10;#     #&#10;#  $  #&#10;# .@. #&#10;#######"
                    />
                    <button
                        type="submit"
                        disabled={isSubmitting || !content.trim()}
                        className="w-full bg-green-600 hover:bg-green-700 text-white font-semibold py-3 px-4 rounded-lg transition-colors shadow-sm disabled:opacity-50 flex justify-center items-center gap-2"
                    >
                        {isSubmitting ? <><Loader2 size={18} className="animate-spin" /> Validating...</> : 'Upload Map'}
                    </button>
                </form>
            </div>
        </div>
    );
}