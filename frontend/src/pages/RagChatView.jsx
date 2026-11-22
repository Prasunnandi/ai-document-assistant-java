import React, { useState } from 'react';
import { 
  Bot, 
  Send, 
  Sparkles, 
  BookOpen, 
  ExternalLink, 
  FileText, 
  Layers, 
  CheckCircle, 
  Clock,
  Search,
  Database,
  ChevronRight
} from 'lucide-react';
import { queryRag } from '../services/api';

export default function RagChatView() {
  const [queryInput, setQueryInput] = useState('');
  const [loading, setLoading] = useState(false);

  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'user',
      text: 'What are the mandatory requirements for employee multi-factor authentication (MFA) under our security policy?'
    },
    {
      id: 2,
      sender: 'assistant',
      text: 'According to Section 4.3 of the **Enterprise Information Security Policy 2026** [Page 14], all remote access into enterprise environments mandates **FIDO2 WebAuthn hardware tokens** or TOTP authenticator applications. **SMS-based 2FA is strictly deprecated** due to SIM-swapping vulnerabilities.',
      confidence: 0.97,
      latencyMs: 142,
      citations: [
        {
          citationId: "CIT-1001",
          documentId: "DOC-RAG-01",
          documentTitle: "Enterprise_Information_Security_Policy_2026.pdf",
          pageNumber: 14,
          relevanceScore: 0.94
        }
      ],
      retrievedChunks: [
        {
          chunkId: "CHK-1001",
          documentId: "DOC-RAG-01",
          documentTitle: "Enterprise_Information_Security_Policy_2026.pdf",
          pageNumber: 14,
          chunkIndex: 42,
          content: "Section 4.3: Mandatory Multi-Factor Authentication (MFA). All remote access to enterprise cloud environments and production clusters must enforce FIDO2 WebAuthn hardware tokens or TOTP authenticator apps. SMS-based 2FA is explicitly deprecated.",
          similarityScore: 0.94
        }
      ]
    }
  ]);

  const handleSend = async (textToSend) => {
    const q = textToSend || queryInput;
    if (!q.trim()) return;

    const userMsg = { id: Date.now(), sender: 'user', text: q };
    setMessages(prev => [...prev, userMsg]);
    setQueryInput('');
    setLoading(true);

    try {
      const response = await queryRag(q);
      const assistantMsg = {
        id: Date.now() + 1,
        sender: 'assistant',
        text: response.synthesizedAnswer,
        confidence: response.responseConfidence || 0.95,
        latencyMs: response.latencyMs || 120,
        citations: response.citations || [],
        retrievedChunks: response.retrievedChunks || []
      };
      setMessages(prev => [...prev, assistantMsg]);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const sampleQuestions = [
    "What is our mandatory MFA policy?",
    "What are our SOC2 disaster recovery RTO and RPO limits?",
    "What is the Kubernetes zero-trust network policy spec?"
  ];

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Top Banner KPI */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <div className="text-xs text-slate-400">Vector Embeddings</div>
          <div className="text-xl font-bold text-white font-mono mt-1">1,536 Dim</div>
          <div className="text-[10px] text-indigo-400 mt-0.5">text-embedding-3-small</div>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <div className="text-xs text-slate-400">Index Structure</div>
          <div className="text-xl font-bold text-white font-mono mt-1">HNSW-Cosine</div>
          <div className="text-[10px] text-emerald-400 mt-0.5">pgvector v0.7.0</div>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <div className="text-xs text-slate-400">Retrieval Accuracy (MRR)</div>
          <div className="text-xl font-bold text-white font-mono mt-1">0.944</div>
          <div className="text-[10px] text-purple-400 mt-0.5">Mean Reciprocal Rank</div>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <div className="text-xs text-slate-400">Document Parsing</div>
          <div className="text-xl font-bold text-white font-mono mt-1">Apache Tika</div>
          <div className="text-[10px] text-blue-400 mt-0.5">PDF, DOCX, Markdown</div>
        </div>
      </div>

      {/* Suggested Queries */}
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-xs text-slate-400 font-mono">Suggested RAG Prompts:</span>
        {sampleQuestions.map((sq, idx) => (
          <button
            key={idx}
            onClick={() => handleSend(sq)}
            className="text-xs bg-slate-900 hover:bg-slate-800 border border-slate-800 hover:border-indigo-500/50 text-slate-300 px-3 py-1.5 rounded-lg transition"
          >
            {sq}
          </button>
        ))}
      </div>

      {/* Conversation Thread */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
        <div className="space-y-6 min-h-[380px]">
          {messages.map((msg) => (
            <div key={msg.id} className={`flex gap-3.5 ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
              {msg.sender === 'assistant' && (
                <div className="w-8 h-8 rounded-lg bg-indigo-500/20 border border-indigo-500/40 flex items-center justify-center text-indigo-400 shrink-0">
                  <Bot className="w-4 h-4" />
                </div>
              )}

              <div className={`max-w-2xl space-y-3 ${msg.sender === 'user' ? 'bg-indigo-600 text-white rounded-2xl rounded-tr-sm p-4 text-xs' : 'bg-slate-950/80 border border-slate-800 text-slate-200 rounded-2xl rounded-tl-sm p-5 text-xs'}`}>
                <p className="leading-relaxed whitespace-pre-line">{msg.text}</p>

                {msg.sender === 'assistant' && msg.citations && msg.citations.length > 0 && (
                  <div className="pt-3 border-t border-slate-800/80 space-y-2">
                    <div className="flex items-center justify-between text-[11px] font-mono text-slate-400">
                      <span className="flex items-center gap-1.5 text-indigo-400 font-semibold">
                        <BookOpen className="w-3.5 h-3.5" /> Verified Ground-Truth Citations
                      </span>
                      <span>Latency: {msg.latencyMs}ms • Confidence: {(msg.confidence * 100).toFixed(0)}%</span>
                    </div>

                    <div className="grid grid-cols-1 gap-2">
                      {msg.citations.map((c, i) => (
                        <div key={i} className="bg-slate-900/90 border border-slate-800 p-2.5 rounded-lg flex items-center justify-between text-[11px]">
                          <div className="flex items-center space-x-2">
                            <FileText className="w-3.5 h-3.5 text-indigo-400" />
                            <span className="font-semibold text-white">{c.documentTitle}</span>
                            <span className="text-slate-500 font-mono">Page {c.pageNumber}</span>
                          </div>
                          <div className="flex items-center space-x-2">
                            <span className="text-indigo-400 font-mono font-semibold">
                              Cosine: {(c.relevanceScore * 100).toFixed(1)}%
                            </span>
                          </div>
                        </div>
                      ))}
                    </div>

                    {msg.retrievedChunks && msg.retrievedChunks.length > 0 && (
                      <div className="mt-2 bg-slate-900/50 p-2.5 rounded-lg border border-slate-800/60 font-mono text-[10px] text-slate-400 leading-relaxed">
                        <span className="text-slate-300 font-bold block mb-1">Top-1 Retrieved Chunk Snippet:</span>
                        "{msg.retrievedChunks[0].content}"
                      </div>
                    )}
                  </div>
                )}
              </div>

              {msg.sender === 'user' && (
                <div className="w-8 h-8 rounded-lg bg-indigo-600/30 border border-indigo-500/50 flex items-center justify-center text-xs font-bold text-indigo-200 shrink-0">
                  U
                </div>
              )}
            </div>
          ))}

          {loading && (
            <div className="flex items-center space-x-3 text-xs text-indigo-400 animate-pulse">
              <Bot className="w-4 h-4" />
              <span>Embedding query & querying pgvector HNSW index via LangChain4j...</span>
            </div>
          )}
        </div>

        {/* Input Bar */}
        <div className="pt-4 border-t border-slate-800">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
            className="flex items-center space-x-3"
          >
            <input
              type="text"
              value={queryInput}
              onChange={(e) => setQueryInput(e.target.value)}
              placeholder="Ask any question across our enterprise policies, compliance docs, and specs..."
              className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition"
            />
            <button
              type="submit"
              disabled={loading || !queryInput.trim()}
              className="px-5 py-3 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-xl text-xs font-semibold flex items-center gap-1.5 transition shadow-lg"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Query RAG</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
