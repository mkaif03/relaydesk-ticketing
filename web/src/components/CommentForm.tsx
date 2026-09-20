"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function CommentForm({ crId }: { crId: string }) {
  const [newComment, setNewComment] = useState("");
  const router = useRouter();

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
      router.refresh();
    }
  };

  return (
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
  );
}
