import Link from "next/link";
import CRForm from "@/components/CRForm";

export default function NewChangeRequestPage() {
  return (
    <div className="min-h-screen p-8 max-w-3xl mx-auto">
      <header className="flex items-center gap-4 mb-8">
        <Link href="/" className="text-gray-400 hover:text-white transition-colors">
          &larr; Back
        </Link>
        <h1 className="text-2xl font-bold text-white">New Change Request</h1>
      </header>

      <CRForm isEdit={false} />
    </div>
  );
}
