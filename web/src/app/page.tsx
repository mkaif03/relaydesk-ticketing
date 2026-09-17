"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import CRDetailsModal from "@/components/CRDetailsModal";

interface ChangeRequest {
  id: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  riskLevel: string;
  service: string;
  environment: string;
  createdAt: string;
}

export default function DashboardPage() {
  const router = useRouter();
  const [crs, setCrs] = useState<ChangeRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCrId, setSelectedCrId] = useState<string | null>(null);
  const [userRole, setUserRole] = useState<string | null>(null);

  const fetchDashboardData = () => {
    setLoading(true);
    fetch("/api/v1/change-requests")
      .then((res) => {
        if (res.status === 401) {
          router.push("/login");
          return null;
        }
        return res.json();
      })
      .then((data) => {
        if (data) setCrs(data);
      })
      .finally(() => setLoading(false));
  };

  const fetchUser = () => {
    fetch("/api/v1/auth/me")
      .then(res => {
        if (res.ok) return res.json();
        return null;
      })
      .then(data => {
        if (data) setUserRole(data.role);
      });
  };

  useEffect(() => {
    fetchDashboardData();
    fetchUser();
  }, [router]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'bg-gray-500/20 text-gray-300 border-gray-500/30';
      case 'IN_REVIEW': return 'bg-yellow-500/20 text-yellow-300 border-yellow-500/30';
      case 'APPROVED': return 'bg-green-500/20 text-green-300 border-green-500/30';
      case 'REJECTED': return 'bg-red-500/20 text-red-300 border-red-500/30';
      case 'SCHEDULED': return 'bg-blue-500/20 text-blue-300 border-blue-500/30';
      default: return 'bg-gray-500/20 text-gray-300 border-gray-500/30';
    }
  };

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }

  return (
    <div className="min-h-screen p-8 max-w-7xl mx-auto">
      <header className="flex justify-between items-center mb-12">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white mb-2">Change Requests</h1>
          <p className="text-gray-400 text-sm">Manage and track your engineering changes.</p>
        </div>
        <div className="flex gap-4">
          {userRole === 'ROLE_ADMIN' && (
            <Link
              href="/admin/audit"
              className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white glass-panel rounded-lg transition-colors border border-gray-500/30 flex items-center"
            >
              Audit Logs
            </Link>
          )}
          <Link
            href="/cr/new"
            className="px-4 py-2 text-sm font-medium text-white bg-[var(--color-primary-500)] hover:bg-[var(--color-primary-600)] rounded-lg transition-colors shadow-lg shadow-[var(--color-primary-500)]/20 flex items-center"
          >
            New Request
          </Link>
          <button
            onClick={() => {
              fetch("/api/v1/auth/logout", { method: "POST" }).then(() => router.push("/login"));
            }}
            className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white glass-panel rounded-lg transition-colors"
          >
            Sign Out
          </button>
        </div>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <div className="glass-panel p-6 rounded-2xl flex flex-col justify-center items-start relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-[var(--color-primary-500)] opacity-10 blur-[40px] rounded-full translate-x-8 -translate-y-8"></div>
          <span className="text-gray-400 text-sm font-medium mb-1 relative z-10">Total Active Requests</span>
          <span className="text-4xl font-bold text-white relative z-10">{crs.length}</span>
        </div>
        
        <div className="glass-panel p-6 rounded-2xl flex flex-col justify-center items-start relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-yellow-500 opacity-10 blur-[40px] rounded-full translate-x-8 -translate-y-8"></div>
          <span className="text-gray-400 text-sm font-medium mb-1 relative z-10">Pending Review</span>
          <span className="text-4xl font-bold text-white relative z-10">{crs.filter(c => c.status === 'IN_REVIEW').length}</span>
        </div>
        
        <div className="glass-panel p-6 rounded-2xl flex flex-col justify-center items-start relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-green-500 opacity-10 blur-[40px] rounded-full translate-x-8 -translate-y-8"></div>
          <span className="text-gray-400 text-sm font-medium mb-1 relative z-10">Approved</span>
          <span className="text-4xl font-bold text-white relative z-10">{crs.filter(c => c.status === 'APPROVED').length}</span>
        </div>
      </div>


      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {crs.map(cr => (
          <div key={cr.id} onClick={() => setSelectedCrId(cr.id)} className="block group cursor-pointer">
            <div className="glass-panel p-6 rounded-2xl transition-all duration-300 group-hover:-translate-y-1 group-hover:shadow-[0_8px_30px_rgb(99,102,241,0.15)] relative overflow-hidden h-full">
              <div className="absolute top-0 left-0 w-1 h-full bg-[var(--color-primary-500)] opacity-0 group-hover:opacity-100 transition-opacity"></div>
              
              <div className="flex justify-between items-start mb-4">
                <span className={`px-2.5 py-1 text-xs font-semibold rounded-full border ${getStatusColor(cr.status)}`}>
                  {cr.status}
                </span>
                <span className="text-xs font-medium px-2 py-1 bg-white/5 rounded text-gray-400">
                  {cr.priority}
                </span>
              </div>
              
              <h2 className="text-lg font-semibold text-white mb-2 group-hover:text-[var(--color-primary-100)] transition-colors">{cr.title}</h2>
              <p className="text-sm text-gray-400 line-clamp-2">{cr.description}</p>
              
              <div className="mt-6 flex flex-col gap-2 text-xs text-gray-500">
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 bg-white/5 rounded text-[var(--color-primary-100)]">{cr.service || 'N/A'}</span>
                  <span className="px-2 py-0.5 bg-white/5 rounded text-gray-300">{cr.environment || 'N/A'}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span>Risk: {cr.riskLevel}</span>
                  <span>{new Date(cr.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
            </div>
          </div>
        ))}

        {crs.length === 0 && (
          <div className="col-span-full glass-panel p-12 rounded-2xl text-center">
            <p className="text-gray-400 mb-4">No change requests found.</p>
          </div>
        )}
      </div>

      {selectedCrId && (
        <CRDetailsModal 
          crId={selectedCrId} 
          onClose={() => setSelectedCrId(null)} 
          onUpdate={fetchDashboardData} 
        />
      )}
    </div>
  );
}
