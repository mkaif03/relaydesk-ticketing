"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

interface CRDetailsModalProps {
  crId: string;
  onClose: () => void;
  onUpdate: () => void; // callback to refresh the dashboard if CR changes
}

export default function CRDetailsModal({ crId, onClose, onUpdate }: CRDetailsModalProps) {
  const [cr, setCr] = useState<any>(null);
  const [reviews, setReviews] = useState<any[]>([]);
  const [comments, setComments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [newComment, setNewComment] = useState("");
  const [isClosing, setIsClosing] = useState(false);

  const fetchData = async () => {
    try {
      setLoading(true);
      const crRes = await fetch(`/api/v1/change-requests/${crId}`);
      if (!crRes.ok) throw new Error("Failed to fetch CR");
      const crData = await crRes.json();
      setCr(crData);

      const reviewsRes = await fetch(`/api/v1/change-requests/${crId}/reviews`);
      setReviews(await reviewsRes.json());

      const commentsRes = await fetch(`/api/v1/change-requests/${crId}/comments`);
      setComments(await commentsRes.json());
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // Prevent body scroll
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [crId]);

  const handleClose = () => {
    setIsClosing(true);
    setTimeout(onClose, 300); // Wait for transition
  };

  const handleTransition = async (targetStatus: string) => {
    const res = await fetch(`/api/v1/change-requests/${crId}/transition`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: targetStatus, version: cr.version }),
    });
    if (res.ok) {
      fetchData();
      onUpdate();
    } else {
      const data = await res.json();
      alert("Failed: " + data.message);
    }
  };

  const submitReview = async (status: string) => {
    const res = await fetch(`/api/v1/change-requests/${crId}/reviews`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status, notes: "" }),
    });
    if (res.ok) {
        fetchData();
        onUpdate();
    }
  };
  
  const submitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    const res = await fetch(`/api/v1/change-requests/${crId}/comments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content: newComment }),
    });
    if (res.ok) {
      setNewComment("");
      fetchData();
    }
  };

  // Close when clicking outside the panel
  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      handleClose();
    }
  };

  return (
    <div 
      className={`fixed inset-0 z-50 flex justify-end transition-opacity duration-300 ${isClosing ? "opacity-0" : "opacity-100"}`}
      style={{ backgroundColor: 'rgba(0, 0, 0, 0.5)', backdropFilter: 'blur(4px)' }}
      onClick={handleBackdropClick}
    >
      <div 
        className={`w-full max-w-2xl h-full bg-[#0d1117] border-l border-white/10 shadow-2xl overflow-y-auto transform transition-transform duration-300 flex flex-col ${isClosing ? "translate-x-full" : "translate-x-0"}`}
      >
        {/* Header */}
        <div className="sticky top-0 z-10 bg-[#0d1117]/90 backdrop-blur-md border-b border-white/10 p-6 flex justify-between items-center">
            <div>
                <h2 className="text-xl font-bold text-white mb-1">
                    {loading ? "Loading..." : cr?.title || "Ticket Details"}
                </h2>
                {cr && (
                    <div className="flex items-center gap-3">
                        <span className="px-2.5 py-1 bg-[var(--color-primary-500)]/20 text-[var(--color-primary-100)] border border-[var(--color-primary-500)]/30 rounded-full text-xs font-bold uppercase tracking-wider">
                            {cr.status}
                        </span>
                        <span className="text-xs text-gray-400 font-mono">{cr.publicKey}</span>
                    </div>
                )}
            </div>
            
            <div className="flex items-center gap-4">
                <Link 
                    href={`/cr/${crId}`}
                    className="text-xs text-[var(--color-primary-400)] hover:text-[var(--color-primary-300)] flex items-center gap-1 transition-colors"
                >
                    View Full Page <span aria-hidden="true">&rarr;</span>
                </Link>
                <button 
                    onClick={handleClose}
                    className="p-2 text-gray-400 hover:text-white bg-white/5 hover:bg-white/10 rounded-full transition-colors"
                    aria-label="Close"
                >
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </button>
            </div>
        </div>

        {/* Body */}
        <div className="flex-1 p-6 space-y-8">
            {loading ? (
                <div className="flex items-center justify-center h-40">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-white"></div>
                </div>
            ) : cr ? (
                <>
                    {/* Metadata Grid */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 p-5 bg-white/5 border border-white/5 rounded-xl">
                        <div>
                            <p className="text-xs text-gray-500 mb-1">Service</p>
                            <p className="font-medium text-white text-sm">{cr.service || 'N/A'}</p>
                        </div>
                        <div>
                            <p className="text-xs text-gray-500 mb-1">Environment</p>
                            <p className="font-medium text-white text-sm">{cr.environment || 'N/A'}</p>
                        </div>
                        <div>
                            <p className="text-xs text-gray-500 mb-1">Priority</p>
                            <p className="font-medium text-white text-sm">{cr.priority}</p>
                        </div>
                        <div>
                            <p className="text-xs text-gray-500 mb-1">Risk Level</p>
                            <p className="font-medium text-white text-sm">{cr.riskLevel}</p>
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="flex flex-wrap gap-3">
                        {cr.status === "DRAFT" && (
                            <button onClick={() => handleTransition("IN_REVIEW")} className="px-4 py-2 bg-yellow-500/20 text-yellow-300 border border-yellow-500/50 hover:bg-yellow-500/30 rounded-lg transition-colors text-sm font-medium">
                            Submit for Review
                            </button>
                        )}
                        {cr.status === "IN_REVIEW" && (
                            <>
                            <button onClick={() => submitReview("APPROVED")} className="px-4 py-2 bg-green-500/20 text-green-300 border border-green-500/50 hover:bg-green-500/30 rounded-lg transition-colors text-sm font-medium">
                                Approve CR
                            </button>
                            <button onClick={() => handleTransition("APPROVED")} className="px-4 py-2 bg-blue-500/20 text-blue-300 border border-blue-500/50 hover:bg-blue-500/30 rounded-lg transition-colors ml-auto text-sm font-medium">
                                Transition to Approved
                            </button>
                            </>
                        )}
                    </div>

                    {/* Description */}
                    <div>
                        <h3 className="text-sm font-semibold text-gray-400 mb-3 uppercase tracking-wider">Description</h3>
                        <div className="prose prose-sm prose-invert prose-p:text-gray-300 prose-headings:text-white max-w-none bg-black/30 p-5 rounded-xl border border-white/5">
                            <ReactMarkdown remarkPlugins={[remarkGfm]}>{cr.description}</ReactMarkdown>
                        </div>
                    </div>

                    {/* Activity */}
                    <div>
                        <h3 className="text-sm font-semibold text-gray-400 mb-4 uppercase tracking-wider">Activity & Comments</h3>
                        <div className="space-y-4 mb-6">
                            {reviews.map(r => (
                                <div key={r.id} className="p-4 bg-white/5 rounded-xl border border-white/5 transition-all hover:bg-white/10">
                                    <div className="flex justify-between items-center mb-2">
                                        <span className="text-sm font-medium text-gray-200">Review</span>
                                        <span className="text-xs text-gray-500">{new Date(r.createdAt).toLocaleString()}</span>
                                    </div>
                                    <span className={`text-xs px-2.5 py-1 rounded-md font-bold ${r.status === 'APPROVED' ? 'bg-green-500/20 text-green-400 border border-green-500/30' : 'bg-red-500/20 text-red-400 border border-red-500/30'}`}>
                                        {r.status}
                                    </span>
                                </div>
                            ))}

                            {comments.map(c => (
                                <div key={c.id} className="p-4 bg-white/5 rounded-xl border border-white/5 transition-all hover:bg-white/10">
                                    <div className="flex justify-between items-center mb-2">
                                        <span className="text-sm font-medium text-[var(--color-primary-300)]">Comment</span>
                                        <span className="text-xs text-gray-500">{new Date(c.createdAt).toLocaleString()}</span>
                                    </div>
                                    <p className="text-sm text-gray-300">{c.content}</p>
                                </div>
                            ))}
                            {reviews.length === 0 && comments.length === 0 && (
                                <p className="text-sm text-gray-500 italic">No activity yet.</p>
                            )}
                        </div>

                        <form onSubmit={submitComment}>
                            <textarea
                                value={newComment}
                                onChange={(e) => setNewComment(e.target.value)}
                                placeholder="Add a comment..."
                                className="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[var(--color-primary-500)] focus:ring-1 focus:ring-[var(--color-primary-500)] resize-none mb-3 transition-all"
                                rows={3}
                            />
                            <div className="flex justify-end">
                                <button
                                    type="submit"
                                    disabled={!newComment.trim()}
                                    className="px-6 py-2 bg-[var(--color-primary-600)] hover:bg-[var(--color-primary-500)] text-white text-sm font-medium rounded-lg transition-colors shadow-[0_0_15px_rgba(79,70,229,0.2)] disabled:opacity-50 disabled:shadow-none"
                                >
                                    Post Comment
                                </button>
                            </div>
                        </form>
                    </div>
                </>
            ) : (
                <div className="text-center text-red-400">Failed to load CR</div>
            )}
        </div>
      </div>
    </div>
  );
}
