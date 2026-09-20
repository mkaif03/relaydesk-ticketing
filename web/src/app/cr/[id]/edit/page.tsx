import Link from "next/link";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import CRForm from "@/components/CRForm";

export default async function EditChangeRequestPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  const apiUrl = process.env.API_URL || "http://localhost:8080";
  const cookieStore = await cookies();
  const jsessionid = cookieStore.get("rd_at");
  
  const headers: Record<string, string> = jsessionid ? { Cookie: `rd_at=${jsessionid.value}` } : {};

  const res = await fetch(`${apiUrl}/api/v1/change-requests/${id}`, {
    headers,
    cache: "no-store",
  });

  if (res.status === 401) {
    redirect("/login");
  }

  if (!res.ok) {
    return <div className="min-h-screen flex items-center justify-center text-red-400">Failed to load CR</div>;
  }

  const cr = await res.json();

  return (
    <div className="min-h-screen p-8 max-w-3xl mx-auto">
      <header className="flex items-center gap-4 mb-8">
        <Link href={`/cr/${id}`} className="text-gray-400 hover:text-white transition-colors">
          &larr; Back
        </Link>
        <h1 className="text-2xl font-bold text-white">Edit Change Request</h1>
      </header>

      <CRForm initialData={cr} isEdit={true} />
    </div>
  );
}
