import React, { useState, useEffect } from 'react';
import { 
  BookOpen, 
  FileText, 
  Search, 
  Database, 
  CheckCircle, 
  Layers, 
  Sparkles, 
  RefreshCw,
  Cpu
} from 'lucide-react';
import { fetchDocuments } from '../services/api';

export default function KnowledgeBaseView() {
  const [documents, setDocuments] = useState([]);
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchDocuments().then(data => setDocuments(data));
  }, []);

  const filtered = documents.filter(d => 
    d.title.toLowerCase().includes(search.toLowerCase()) ||
    d.category.toLowerCase().includes(search.toLowerCase()) ||
    d.embeddingModel.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-white tracking-wide flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-indigo-400" />
            Indexed Knowledge Documents & Vector Storage
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Enterprise corpus parsed with Apache Tika, chunked with LangChain4j, and embedded into pgvector HNSW tables.
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <span className="text-[11px] font-mono bg-indigo-500/10 text-indigo-300 border border-indigo-500/30 px-3 py-1 rounded-md flex items-center gap-1.5">
            <Cpu className="w-3.5 h-3.5 text-indigo-400" />
            pgvector HNSW: Active
          </span>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl flex items-center justify-between gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search documents, category, or embedding models..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-800 border border-slate-700 rounded-lg pl-9 pr-4 py-1.5 text-xs text-white focus:outline-none focus:border-indigo-500"
          />
        </div>
        <div className="text-xs font-mono text-slate-400">
          Showing <span className="text-indigo-400 font-bold">{filtered.length}</span> indexed documents
        </div>
      </div>

      {/* Documents Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-lg">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/70 text-slate-400 uppercase font-mono border-b border-slate-800 text-[11px]">
              <tr>
                <th className="py-3 px-4">Document Title & ID</th>
                <th className="py-3 px-4">Category</th>
                <th className="py-3 px-4">Pages / Chunks</th>
                <th className="py-3 px-4">Embedding Model</th>
                <th className="py-3 px-4">Index & MRR</th>
                <th className="py-3 px-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-sans">
              {filtered.map((doc) => (
                <tr key={doc.documentId} className="hover:bg-slate-800/40 transition">
                  <td className="py-3.5 px-4">
                    <div className="flex items-center space-x-3">
                      <div className="p-2 bg-indigo-500/10 text-indigo-400 rounded-lg">
                        <FileText className="w-4 h-4" />
                      </div>
                      <div>
                        <div className="font-semibold text-white flex items-center gap-2">
                          {doc.title}
                          <span className="font-mono text-[10px] text-slate-400 bg-slate-800 px-1.5 py-0.5 rounded">
                            {doc.format}
                          </span>
                        </div>
                        <div className="text-[10px] font-mono text-slate-400 mt-0.5">
                          ID: {doc.documentId}
                        </div>
                      </div>
                    </div>
                  </td>

                  <td className="py-3.5 px-4 text-slate-300">
                    {doc.category}
                  </td>

                  <td className="py-3.5 px-4 font-mono text-slate-300">
                    {doc.pageCount} Pages <span className="text-slate-500">/</span> {doc.chunkCount} Chunks
                  </td>

                  <td className="py-3.5 px-4">
                    <div className="font-mono text-indigo-300 text-[11px]">{doc.embeddingModel}</div>
                    <div className="text-[10px] text-slate-400">{doc.vectorDim} dimensions</div>
                  </td>

                  <td className="py-3.5 px-4">
                    <div className="font-mono text-white font-semibold">MRR: {doc.retrievalMrr}</div>
                    <div className="text-[10px] text-emerald-400 font-mono">{doc.indexType}</div>
                  </td>

                  <td className="py-3.5 px-4">
                    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                      <CheckCircle className="w-3 h-3" />
                      {doc.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
