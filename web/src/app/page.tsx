import Link from "next/link";
import { cookies } from "next/headers";
import FilterControls from "@/components/FilterControls";
import DashboardActions from "@/components/DashboardActions";

export default async function DashboardPage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | undefined }>;
}) {
  const params = await searchParams;
  const page = params.page || "0";
  const status = params.status || "";
  const riskLevel = params.riskLevel || "";
  const environment = params.environment || "";
  const authorEmail = params.authorEmail || "";
  const q = params.q || "";

  const apiUrl = process.env.API_URL || "http://localhost:8080";
  const cookieStore = await cookies();
  const jsessionid = cookieStore.get("rd_at");

  const queryParams = new URLSearchParams();
  queryParams.append("page", page);
  queryParams.append("size", "12");
  if (status) queryParams.append("status", status);
  if (riskLevel) queryParams.append("riskLevel", riskLevel);
  if (environment) queryParams.append("environment", environment);
  if (authorEmail) queryParams.append("authorEmail", authorEmail);
  if (q) queryParams.append("q", q);

  let crs = [];
  let totalPages = 0;
  let totalElements = 0;

  try {
    const res = await fetch(`${apiUrl}/api/v1/change-requests?${queryParams.toString()}`, {
      headers: (jsessionid ? { Cookie: `rd_at=${jsessionid.value}` } : {}) as Record<string, string>,
      cache: "no-store",
    });

    if (res.ok) {
      const data = await res.json();
      if (data && data.content) {
        crs = data.content;
        totalPages = data.totalPages;
        totalElements = data.totalElements;
      } else if (Array.isArray(data)) {
        crs = data;
        totalPages = 1;
        totalElements = data.length;
      }
    }
  } catch (err) {
    console.error("Failed to fetch dashboard data:", err);
  }

  let currentUser = null;
  try {
    const userRes = await fetch(`${apiUrl}/api/v1/auth/me`, {
      headers: (jsessionid ? { Cookie: `rd_at=${jsessionid.value}` } : {}) as Record<string, string>,
      cache: "no-store",
    });
    if (userRes.ok) {
      currentUser = await userRes.json();
    }
  } catch (err) {
    console.error("Failed to fetch user:", err);
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "DRAFT":
        return "bg-gray-500/20 text-gray-300 border-gray-500/30";
      case "SUBMITTED":
        return "bg-yellow-500/20 text-yellow-300 border-yellow-500/30";
      case "CHANGES_REQUESTED":
        return "bg-orange-500/20 text-orange-300 border-orange-500/30";
      case "APPROVED":
        return "bg-green-500/20 text-green-300 border-green-500/30";
      case "REJECTED":
        return "bg-red-500/20 text-red-300 border-red-500/30";
      case "CANCELLED":
        return "bg-gray-600/20 text-gray-400 border-gray-600/30";
      case "SCHEDULED":
        return "bg-blue-500/20 text-blue-300 border-blue-500/30";
      case "SHIPPED":
        return "bg-purple-500/20 text-purple-300 border-purple-500/30";
      default:
        return "bg-gray-500/20 text-gray-300 border-gray-500/30";
    }
  };

  const pageNum = parseInt(page, 10);

  return (
    <div className="min-h-screen p-8 max-w-7xl mx-auto">
      <header className="flex justify-between items-center mb-12">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white mb-2">Change Requests</h1>
          <p className="text-gray-400 text-sm">Manage and track your engineering changes.</p>
        </div>
        <DashboardActions currentUser={currentUser} />
      </header>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="glass-panel p-6 rounded-2xl flex flex-col justify-center items-start relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-[var(--color-primary-500)] opacity-10 blur-[40px] rounded-full translate-x-8 -translate-y-8"></div>
          <span className="text-gray-400 text-sm font-medium mb-1 relative z-10">Total Requests Found</span>
          <span className="text-4xl font-bold text-white relative z-10">{totalElements}</span>
        </div>
      </div>

      {/* Filter Bar */}
      <FilterControls filters={{ status, riskLevel, environment, authorEmail, q }} />

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        {crs.map((cr: any) => (
          <Link href={`/cr/${cr.id}`} key={cr.id} className="block group cursor-pointer h-full">
            <div className="glass-panel p-6 rounded-2xl transition-all duration-300 group-hover:-translate-y-1 group-hover:shadow-[0_8px_30px_rgb(99,102,241,0.15)] relative overflow-hidden h-full flex flex-col">
              <div className="absolute top-0 left-0 w-1 h-full bg-[var(--color-primary-500)] opacity-0 group-hover:opacity-100 transition-opacity"></div>

              <div className="flex justify-between items-start mb-4">
                <span className={`px-2.5 py-1 text-xs font-semibold rounded-full border ${getStatusColor(cr.status)}`}>
                  {cr.status}
                </span>
                <span className="text-xs font-medium px-2 py-1 bg-white/5 rounded text-gray-400">
                  {cr.priority}
                </span>
              </div>

              <h2 className="text-lg font-semibold text-white mb-2 group-hover:text-[var(--color-primary-100)] transition-colors">
                {cr.title}
              </h2>
              <p className="text-sm text-gray-400 line-clamp-2 flex-grow">{cr.description}</p>

              <div className="mt-6 flex flex-col gap-2 text-xs text-gray-500">
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 bg-white/5 rounded text-[var(--color-primary-100)]">
                    {cr.service || "N/A"}
                  </span>
                  <span className="px-2 py-0.5 bg-white/5 rounded text-gray-300">{cr.environment || "N/A"}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span>Risk: {cr.riskLevel}</span>
                  <span>{new Date(cr.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
            </div>
          </Link>
        ))}
      </div>

      {crs.length === 0 && (
        <div className="glass-panel p-12 rounded-2xl text-center">
          <p className="text-gray-400 mb-4">No change requests found matching your filters.</p>
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between glass-panel px-6 py-4 rounded-xl">
          <div className="text-sm text-gray-400">
            Showing page <span className="font-semibold text-white">{pageNum + 1}</span> of{" "}
            <span className="font-semibold text-white">{totalPages}</span>
          </div>
          <div className="flex gap-2">
            <Link
              href={`/?${new URLSearchParams({ ...params, page: Math.max(0, pageNum - 1).toString() } as any).toString()}`}
              className={`px-4 py-2 text-sm font-medium text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors ${
                pageNum === 0 ? "opacity-50 pointer-events-none" : ""
              }`}
            >
              Previous
            </Link>
            <Link
              href={`/?${new URLSearchParams({ ...params, page: Math.min(totalPages - 1, pageNum + 1).toString() } as any).toString()}`}
              className={`px-4 py-2 text-sm font-medium text-white bg-white/5 hover:bg-white/10 rounded-lg transition-colors ${
                pageNum >= totalPages - 1 ? "opacity-50 pointer-events-none" : ""
              }`}
            >
              Next
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
