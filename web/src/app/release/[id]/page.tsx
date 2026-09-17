"use client";

import { useEffect, useState, use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function ReleaseDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const router = useRouter();
  const { id } = use(params);
  const [release, setRelease] = useState<any>(null);
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try {
      const relRes = await fetch(`/api/v1/releases/${id}`);
      if (relRes.status === 401) return router.push("/login");
      setRelease(await relRes.json());

      const itemsRes = await fetch(`/api/v1/releases/${id}/items`);
      setItems(await itemsRes.json());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const shipRelease = async () => {
    const res = await fetch(`/api/v1/releases/${id}/ship?version=${release.version}`, { method: "POST" });
    if (res.ok) fetchData();
    else alert("Failed to ship: " + (await res.json()).message);
  };

  const rollbackRelease = async () => {
    const res = await fetch(`/api/v1/releases/${id}/rollback?version=${release.version}`, { method: "POST" });
    if (res.ok) fetchData();
    else alert("Failed to rollback: " + (await res.json()).message);
  };

  if (loading) return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  if (!release) return <div>Not Found</div>;

  return (
    <div className="min-h-screen p-8 max-w-7xl mx-auto">
      <Link href="/" className="text-gray-400 hover:text-white flex items-center gap-2 mb-8 transition-colors w-fit">
        ← Back to Dashboard
      </Link>

      <div className="glass-panel p-8 rounded-2xl mb-8 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-32 h-32 bg-[var(--color-primary-500)] rounded-full mix-blend-screen filter blur-[40px] opacity-10"></div>
        
        <div className="flex items-center gap-4 mb-6 relative z-10">
          <h1 className="text-3xl font-bold text-white">{release.name}</h1>
          <span className={`px-3 py-1 text-xs font-bold uppercase tracking-wider rounded-full border ${
            release.status === 'SHIPPED' ? 'bg-green-500/20 text-green-300 border-green-500/30' :
            release.status === 'ROLLED_BACK' ? 'bg-red-500/20 text-red-300 border-red-500/30' :
            'bg-blue-500/20 text-blue-300 border-blue-500/30'
          }`}>
            {release.status}
          </span>
        </div>
        
        <p className="text-gray-300 mb-6 relative z-10">{release.notes || "No notes provided."}</p>
        
        <div className="flex gap-4 relative z-10 pt-4 border-t border-white/10">
          {release.status === "PLANNED" && (
            <button onClick={shipRelease} className="px-6 py-2 bg-[var(--color-primary-600)] hover:bg-[var(--color-primary-500)] text-white font-medium rounded-lg transition-colors shadow-[0_0_15px_rgba(79,70,229,0.3)]">
              Ship Release
            </button>
          )}
          {release.status === "SHIPPED" && (
            <button onClick={rollbackRelease} className="px-6 py-2 bg-red-500/20 text-red-400 hover:bg-red-500/30 font-medium border border-red-500/50 rounded-lg transition-colors">
              Rollback Release
            </button>
          )}
        </div>
      </div>

      <h2 className="text-xl font-semibold text-white mb-4">Included Change Requests</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {items.map(item => (
          <Link href={`/cr/${item.changeRequestId}`} key={item.id} className="block group">
            <div className="glass-panel p-4 rounded-xl transition-all duration-300 hover:bg-white/5 border border-white/5 hover:border-white/10">
              <p className="text-sm text-gray-300">Change Request: <span className="font-mono text-xs">{item.changeRequestId}</span></p>
              <p className="text-xs text-gray-500 mt-2">Added on {new Date(item.createdAt).toLocaleDateString()}</p>
            </div>
          </Link>
        ))}
        {items.length === 0 && (
          <div className="col-span-full text-center p-8 border border-dashed border-white/10 rounded-xl text-gray-500">
            No Change Requests bundled in this release.
          </div>
        )}
      </div>
    </div>
  );
}
