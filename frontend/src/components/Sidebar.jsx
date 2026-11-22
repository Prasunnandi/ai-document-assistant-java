import React from 'react';
import { 
  Bot, 
  BookOpen, 
  Database, 
  Sparkles, 
  Layers, 
  Terminal, 
  Cpu,
  FileText
} from 'lucide-react';

export default function Sidebar({ activeTab, setActiveTab }) {
  const menuItems = [
    { id: 'chat', label: 'Semantic RAG Chat', icon: Bot, badge: 'LangChain4j' },
    { id: 'docs', label: 'Indexed Knowledge Base', icon: BookOpen, badge: '4 Docs' },
  ];

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between shrink-0 h-screen sticky top-0">
      <div>
        {/* Brand */}
        <div className="h-16 px-6 flex items-center space-x-3 border-b border-slate-800">
          <div className="w-9 h-9 rounded-lg bg-indigo-500/20 border border-indigo-500/40 flex items-center justify-center text-indigo-400 font-bold shadow-inner">
            <Bot className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-bold text-white tracking-wide flex items-center gap-1.5">
              RAG Assistant
              <span className="text-[10px] uppercase font-semibold px-1.5 py-0.5 rounded bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                Java 21
              </span>
            </h1>
            <p className="text-[11px] text-slate-400 font-mono">LangChain4j & pgvector</p>
          </div>
        </div>

        {/* Navigation */}
        <nav className="p-3 space-y-1">
          <div className="text-[11px] font-semibold text-slate-400 px-3 py-2 uppercase tracking-wider">
            Workspace
          </div>
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive 
                    ? 'bg-indigo-500/10 text-indigo-400 border border-indigo-500/30 shadow-sm' 
                    : 'text-slate-300 hover:bg-slate-800/60 hover:text-white'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <Icon className={`w-4 h-4 ${isActive ? 'text-indigo-400' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className={`text-[10px] px-1.5 py-0.5 rounded font-mono font-medium ${
                    isActive ? 'bg-indigo-500/20 text-indigo-300' : 'bg-slate-800 text-slate-400'
                  }`}>
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Vector Store Info Footer */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/40 m-3 rounded-xl border">
        <div className="flex items-center space-x-2 text-xs font-semibold text-indigo-400">
          <Database className="w-4 h-4 text-indigo-400" />
          <span>pgvector HNSW Index</span>
        </div>
        <p className="text-[11px] text-slate-400 mt-1 leading-relaxed">
          Embeddings: 1536-dim text-embedding-3-small with recursive token windowing & Apache Tika.
        </p>
        <div className="mt-3 pt-2 border-t border-slate-800 flex items-center justify-between text-[10px] font-mono text-slate-500">
          <span>Spring Boot 3.3.0</span>
          <span className="text-emerald-400 font-bold">Online</span>
        </div>
      </div>
    </aside>
  );
}
