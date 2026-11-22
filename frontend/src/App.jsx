import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';
import RagChatView from './pages/RagChatView';
import KnowledgeBaseView from './pages/KnowledgeBaseView';
import { fetchStats } from './services/api';

export default function App() {
  const [activeTab, setActiveTab] = useState('chat');
  const [stats, setStats] = useState({
    totalDocuments: 4,
    totalIndexedChunks: 1217,
    averageRetrievalMrr: 0.944,
    vectorDimensions: 1536,
    indexAlgorithm: "HNSW (M=16, efConstruction=64)"
  });

  useEffect(() => {
    fetchStats().then(data => {
      if (data) setStats(data);
    });
  }, []);

  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-100 font-sans antialiased">
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />
      <div className="flex-1 flex flex-col min-w-0">
        <Navbar stats={stats} />
        <main className="flex-1 p-6 lg:p-8 overflow-y-auto">
          {activeTab === 'chat' && <RagChatView />}
          {activeTab === 'docs' && <KnowledgeBaseView />}
        </main>
      </div>
    </div>
  );
}
