import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Header } from './components/Header';
import { Dashboard } from './pages/Dashboard';
import { EvidenceList } from './components/EvidenceList';
import { EvidenceDetail } from './pages/EvidenceDetail';
import { CreateEvidence } from './pages/CreateEvidence';
import { IncidentList } from './pages/IncidentList';
import { IncidentDetail } from './pages/IncidentDetail';
import { CreateIncident } from './pages/CreateIncident';
import { AuditLog } from './pages/AuditLog';
import { Ratings } from './pages/Ratings';
import { AuthBoundary } from './auth/AuthBoundary';
import './App.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthBoundary>
        <BrowserRouter>
          <div className="App">
            <Header />
            <main>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/evidence" element={<EvidenceList />} />
                <Route path="/evidence/new" element={<CreateEvidence />} />
                <Route path="/evidence/:id" element={<EvidenceDetail />} />
                <Route path="/incidents" element={<IncidentList />} />
                <Route path="/incidents/new" element={<CreateIncident />} />
                <Route path="/incidents/:id" element={<IncidentDetail />} />
                <Route path="/audit" element={<AuditLog />} />
                <Route path="/ratings" element={<Ratings />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </main>
          </div>
        </BrowserRouter>
      </AuthBoundary>
    </QueryClientProvider>
  );
}

export default App;
