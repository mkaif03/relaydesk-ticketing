"use client";

import { useEffect, useState, use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

export default function CRDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const router = useRouter();
  const { id } = use(params);
  const [cr, setCr] = useState<any>(null);
  const [reviews, setReviews] = useState<any[]>([]);
  const [comments, setComments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [newComment, setNewComment] = useState("");

  const fetchData = async () => {
    try {
      const crRes = await fetch(`/api/v1/change-requests/${id}`);
      if (crRes.status === 401) return router.push("/login");
      const crData = await crRes.json();
      setCr(crData);

      const reviewsRes = await fetch(`/api/v1/change-requests/${id}/reviews`);
      setReviews(await reviewsRes.json());

      const commentsRes = await fetch(`/api/v1/change-requests/${id}/comments`);
      setComments(await commentsRes.json());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleTransition = async (targetStatus: string) => {
    const res = await fetch(`/api/v1/change-requests/${id}/transition`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: targetStatus, version: cr.version }),
    });
    if (res.ok) {
      fetchData(); // reload
    } else {
      const data = await res.json();
      alert("Failed: " + data.message);
    }
  };

  const submitReview = async (status: string) => {
    const res = await fetch(`/api/v1/change-requests/${id}/reviews`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status, notes: "" }),
    });
    if (res.ok) fetchData();
  };
  
  const submitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    const res = await fetch(`/api/v1/change-requests/${id}/comments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content: newComment }),
    });
    if (res.ok) {
      setNewComment("");
      fetchData();
    }
  };

  if (loading) return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  if (!cr) return <div>Not Found</div>;

  return (
    <div className="min-h-screen p-8 max-w-7xl mx-auto">
      <Link href="/" className="text-gray-400 hover:text-white flex items-center gap-2 mb-8 transition-colors w-fit">
        ← Back to Dashboard
      </Link>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left Column: Details */}
        <div className="lg:col-span-2 space-y-6">
          <div className="glass-panel p-8 rounded-2xl">
            <div className="flex items-center justify-between gap-4 mb-6">
              <div className="flex items-center gap-4">
                <h1 className="text-3xl font-bold text-white">{cr.title}</h1>
                <span className="px-3 py-1 bg-[var(--color-primary-500)]/20 text-[var(--color-primary-100)] border border-[var(--color-primary-500)]/30 rounded-full text-xs font-bold uppercase tracking-wider">
                  {cr.status}
                </span>
              </div>
              <div className="flex gap-2">
                <Link
                  href={`/cr/${id}/edit`}
                  className="px-3 py-1.5 bg-white/10 hover:bg-white/20 text-white text-sm font-medium rounded transition-colors"
                >
                  Edit
                </Link>
                <button
                  onClick={async () => {
                    if (confirm("Are you sure you want to delete this Change Request?")) {
                      const res = await fetch(`/api/v1/change-requests/${id}`, { method: "DELETE" });
                      if (res.ok) router.push("/");
                      else alert("Failed to delete CR");
                    }
                  }}
                  className="px-3 py-1.5 bg-red-500/20 text-red-400 hover:bg-red-500/30 border border-red-500/30 text-sm font-medium rounded transition-colors"
                >
                  Delete
                </button>
              </div>
            </div>
            
            <div className="prose prose-invert prose-p:text-gray-300 prose-headings:text-white max-w-none leading-relaxed mb-8">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{cr.description}</ReactMarkdown>
            </div>
            
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4 p-4 bg-white/5 rounded-xl">
              <div>
                <p className="text-xs text-gray-500 mb-1">Service</p>
                <p className="font-medium text-white truncate">{cr.service || 'N/A'}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-1">Environment</p>
                <p className="font-medium text-white">{cr.environment || 'N/A'}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-1">Priority</p>
                <p className="font-medium text-white">{cr.priority}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-1">Risk Level</p>
                <p className="font-medium text-white">{cr.riskLevel}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-1">Created</p>
                <p className="font-medium text-white">{new Date(cr.createdAt).toLocaleDateString()}</p>
              </div>
            </div>

            <div className="mt-8 pt-6 border-t border-white/10 flex flex-wrap gap-3">
              {cr.status === "DRAFT" && (
                <button onClick={() => handleTransition("IN_REVIEW")} className="px-4 py-2 bg-yellow-500/20 text-yellow-300 border border-yellow-500/50 hover:bg-yellow-500/30 rounded-lg transition-colors">
                  Submit for Review
                </button>
              )}
              {cr.status === "IN_REVIEW" && (
                <>
                  <button onClick={() => submitReview("APPROVED")} className="px-4 py-2 bg-green-500/20 text-green-300 border border-green-500/50 hover:bg-green-500/30 rounded-lg transition-colors">
                    Approve CR
                  </button>
                  <button onClick={() => handleTransition("APPROVED")} className="px-4 py-2 bg-blue-500/20 text-blue-300 border border-blue-500/50 hover:bg-blue-500/30 rounded-lg transition-colors ml-auto">
                    Transition to Approved (Requires Review)
                  </button>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Timeline & Comments */}
        <div className="space-y-6">
          <div className="glass-panel p-6 rounded-2xl">
            <h3 className="text-lg font-semibold text-white mb-6">Activity Timeline</h3>
            
            <div className="space-y-4 mb-6">
              {reviews.map(r => (
                <div key={r.id} className="p-3 bg-white/5 rounded-xl border border-white/5 transition-all hover:bg-white/10">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-sm font-medium text-gray-200">Review</span>
                    <span className="text-xs text-gray-500">{new Date(r.createdAt).toLocaleString()}</span>
                  </div>
                  <span className={`text-xs px-2 py-0.5 rounded font-medium ${r.status === 'APPROVED' ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'}`}>
                    {r.status}
                  </span>
                </div>
              ))}

              {comments.map(c => (
                <div key={c.id} className="p-3 bg-white/5 rounded-xl border border-white/5 transition-all hover:bg-white/10">
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-sm font-medium text-[var(--color-primary-100)]">Comment</span>
                    <span className="text-xs text-gray-500">{new Date(c.createdAt).toLocaleString()}</span>
                  </div>
                  <p className="text-sm text-gray-300">{c.content}</p>
                </div>
              ))}
            </div>

            <form onSubmit={submitComment} className="pt-4 border-t border-white/10">
              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder="Add a comment..."
                className="w-full bg-black/20 border border-white/10 rounded-lg p-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[var(--color-primary-500)] focus:ring-1 focus:ring-[var(--color-primary-500)] resize-none mb-3 transition-all"
                rows={3}
              />
              <button
                type="submit"
                disabled={!newComment.trim()}
                className="w-full py-2 bg-[var(--color-primary-600)] hover:bg-[var(--color-primary-500)] text-white text-sm font-medium rounded-lg transition-colors shadow-[0_0_15px_rgba(79,70,229,0.2)] disabled:opacity-50 disabled:shadow-none"
              >
                Post Comment
              </button>
            </form>
          </div>
        </div>
        
      </div>
    </div>
  );
}
