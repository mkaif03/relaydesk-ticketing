import Link from "next/link";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import CRActionButtons from "@/components/CRActionButtons";
import CommentForm from "@/components/CommentForm";

export default async function CRDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  
  const apiUrl = process.env.API_URL || "http://localhost:8080";
  const cookieStore = await cookies();
  const jsessionid = cookieStore.get("rd_at");
  
  const headers: Record<string, string> = jsessionid ? { Cookie: `rd_at=${jsessionid.value}` } : {};

  // Fetch CR
  const crRes = await fetch(`${apiUrl}/api/v1/change-requests/${id}`, { headers, cache: "no-store" });
  if (crRes.status === 401) {
    redirect("/login");
  }
  if (!crRes.ok) {
    return <div>Not Found</div>;
  }
  const cr = await crRes.json();

  // Fetch Reviews
  const reviewsRes = await fetch(`${apiUrl}/api/v1/change-requests/${id}/reviews`, { headers, cache: "no-store" });
  const reviews = reviewsRes.ok ? await reviewsRes.json() : [];

  // Fetch Comments
  const commentsRes = await fetch(`${apiUrl}/api/v1/change-requests/${id}/comments`, { headers, cache: "no-store" });
  const comments = commentsRes.ok ? await commentsRes.json() : [];

  // Fetch User
  const userRes = await fetch(`${apiUrl}/api/v1/auth/me`, { headers, cache: "no-store" });
  let currentUser = null;
  if (userRes.ok) {
    currentUser = await userRes.json();
  }

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
                {(currentUser?.role === 'ROLE_ADMIN' || currentUser?.id === cr.authorId) && (
                  <Link
                    href={`/cr/${id}/edit`}
                    className="px-3 py-1.5 bg-white/10 hover:bg-white/20 text-white text-sm font-medium rounded transition-colors"
                  >
                    Edit
                  </Link>
                )}
                {/* Note: Delete functionality requires a client component if we keep the confirm dialogue. For now we will omit or we can create a DeleteButton component */}
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

            <CRActionButtons cr={cr} currentUser={currentUser} />
          </div>
        </div>

        {/* Right Column: Timeline & Comments */}
        <div className="space-y-6">
          <div className="glass-panel p-6 rounded-2xl">
            <h3 className="text-lg font-semibold text-white mb-6">Activity Timeline</h3>
            
            <div className="space-y-4 mb-6">
              {reviews.map((r: any) => (
                <div key={r.id} className="p-3 bg-white/5 rounded-xl border border-white/5 transition-all hover:bg-white/10">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-sm font-medium text-gray-200">Review</span>
                    <span className="text-xs text-gray-500">{new Date(r.createdAt).toLocaleString()}</span>
                  </div>
                  <span className={`text-xs px-2 py-0.5 rounded font-medium ${r.status === 'APPROVED' ? 'bg-green-500/20 text-green-400' : 'bg-orange-500/20 text-orange-400'}`}>
                    {r.status}
                  </span>
                </div>
              ))}

              {comments.map((c: any) => (
                <div key={c.id} className="p-3 bg-white/5 rounded-xl border border-white/5 transition-all hover:bg-white/10">
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-sm font-medium text-[var(--color-primary-100)]">Comment</span>
                    <span className="text-xs text-gray-500">{new Date(c.createdAt).toLocaleString()}</span>
                  </div>
                  <p className="text-sm text-gray-300">{c.content}</p>
                </div>
              ))}
            </div>

            <CommentForm crId={id} />
          </div>
        </div>
        
      </div>
    </div>
  );
}
