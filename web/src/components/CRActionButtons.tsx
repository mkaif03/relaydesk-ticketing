"use client";

import { useRouter } from "next/navigation";

export default function CRActionButtons({
  cr,
  currentUser,
}: {
  cr: any;
  currentUser: any;
}) {
  const router = useRouter();

  const handleTransition = async (targetStatus: string) => {
    const res = await fetch(`/api/v1/change-requests/${cr.id}/transition`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: targetStatus, version: cr.version }),
    });
    if (res.ok) {
      router.refresh();
    } else {
      const data = await res.json();
      alert("Failed: " + data.message);
    }
  };

  const submitReview = async (status: string) => {
    const res = await fetch(`/api/v1/change-requests/${cr.id}/reviews`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status, notes: "" }),
    });
    if (res.ok) {
      router.refresh();
    }
  };

  return (
    <div className="mt-8 pt-6 border-t border-white/10 flex flex-wrap gap-3">
      {/* Only Authors or Admins can submit drafts */}
      {(cr.status === "DRAFT" || cr.status === "CHANGES_REQUESTED") &&
        (currentUser?.role === "ROLE_ADMIN" || currentUser?.id === cr.authorId) && (
          <button
            onClick={() => handleTransition("SUBMITTED")}
            className="px-4 py-2 bg-yellow-500/20 text-yellow-300 border border-yellow-500/50 hover:bg-yellow-500/30 rounded-lg transition-colors"
          >
            Submit for Review
          </button>
        )}
      {/* Only Reviewers or Admins (who are NOT the author) can approve/reject/request changes */}
      {cr.status === "SUBMITTED" &&
        (currentUser?.role === "ROLE_ADMIN" || currentUser?.role === "ROLE_REVIEWER") &&
        currentUser?.id !== cr.authorId && (
          <>
            <button
              onClick={() => submitReview("APPROVED")}
              className="px-4 py-2 bg-green-500/20 text-green-300 border border-green-500/50 hover:bg-green-500/30 rounded-lg transition-colors"
            >
              Approve CR
            </button>
            <button
              onClick={() => handleTransition("CHANGES_REQUESTED")}
              className="px-4 py-2 bg-orange-500/20 text-orange-300 border border-orange-500/50 hover:bg-orange-500/30 rounded-lg transition-colors"
            >
              Request Changes
            </button>
            <button
              onClick={() => handleTransition("REJECTED")}
              className="px-4 py-2 bg-red-500/20 text-red-300 border border-red-500/50 hover:bg-red-500/30 rounded-lg transition-colors"
            >
              Reject CR
            </button>
          </>
        )}
      {/* Only Reviewers or Admins can transition to APPROVED */}
      {cr.status === "SUBMITTED" &&
        (currentUser?.role === "ROLE_ADMIN" || currentUser?.role === "ROLE_REVIEWER") && (
          <button
            onClick={() => handleTransition("APPROVED")}
            className="px-4 py-2 bg-blue-500/20 text-blue-300 border border-blue-500/50 hover:bg-blue-500/30 rounded-lg transition-colors ml-auto"
          >
            Transition to Approved (Requires Review)
          </button>
        )}
      {cr.status === "APPROVED" && (
        <>
          {/* Only Reviewers or Admins (who are NOT the author) can reject from approved */}
          {(currentUser?.role === "ROLE_ADMIN" || currentUser?.role === "ROLE_REVIEWER") &&
            currentUser?.id !== cr.authorId && (
              <button
                onClick={() => handleTransition("REJECTED")}
                className="px-4 py-2 bg-red-500/20 text-red-300 border border-red-500/50 hover:bg-red-500/30 rounded-lg transition-colors"
              >
                Reject CR
              </button>
            )}
          {/* Author, Reviewer, or Admin can cancel */}
          {(currentUser?.role === "ROLE_ADMIN" ||
            currentUser?.role === "ROLE_REVIEWER" ||
            currentUser?.id === cr.authorId) && (
            <button
              onClick={() => handleTransition("CANCELLED")}
              className="px-4 py-2 bg-gray-600/20 text-gray-400 border border-gray-600/50 hover:bg-gray-600/30 rounded-lg transition-colors"
            >
              Cancel CR
            </button>
          )}
        </>
      )}
    </div>
  );
}
