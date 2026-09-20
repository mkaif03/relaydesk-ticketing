import Link from "next/link";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import ReleaseActionButtons from "@/components/ReleaseActionButtons";

export default async function ReleaseDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  const apiUrl = process.env.API_URL || "http://localhost:8080";
  const cookieStore = await cookies();
  const jsessionid = cookieStore.get("rd_at");
  
  const headers: Record<string, string> = jsessionid ? { Cookie: `rd_at=${jsessionid.value}` } : {};

  const relRes = await fetch(`${apiUrl}/api/v1/releases/${id}`, {
    headers,
    cache: "no-store",
  });

  if (relRes.status === 401) {
    redirect("/login");
  }

  if (!relRes.ok) {
    return <div className="min-h-screen flex items-center justify-center text-red-400">Not Found</div>;
  }

  const release = await relRes.json();

  const itemsRes = await fetch(`${apiUrl}/api/v1/releases/${id}/items`, {
    headers,
    cache: "no-store",
  });
  
  const items = itemsRes.ok ? await itemsRes.json() : [];

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
        
        <ReleaseActionButtons release={release} />
      </div>

      <h2 className="text-xl font-semibold text-white mb-4">Included Change Requests</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {items.map((item: any) => (
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
