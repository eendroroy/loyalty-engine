import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import DataSources from './pages/DataSources';
import DataSourceForm from './pages/DataSourceForm';
import ArchivedSources from './pages/ArchivedSources';
import Rules from './pages/Rules';
import RuleForm from './pages/RuleForm';
import Monitoring from './pages/Monitoring';
import ImportedData from './pages/ImportedData';
import ImportedDataTable from './pages/ImportedDataTable';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/"                          element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard"                 element={<Dashboard />} />
        <Route path="/data-sources"              element={<DataSources />} />
        <Route path="/data-sources/new"          element={<DataSourceForm />} />
        {/* View (read-only) and Edit are separate routes */}
        <Route path="/data-sources/:id"          element={<DataSourceForm readOnly />} />
        <Route path="/data-sources/:id/edit"     element={<DataSourceForm />} />
        <Route path="/archived-sources"          element={<ArchivedSources />} />
        <Route path="/imported-data"             element={<ImportedData />} />
        <Route path="/imported-data/:id"         element={<ImportedDataTable />} />
        <Route path="/rules"                     element={<Rules />} />
        <Route path="/rules/new"                 element={<RuleForm />} />
        <Route path="/rules/:id"                 element={<RuleForm />} />
        <Route path="/monitor"                   element={<Monitoring />} />
      </Routes>
    </Layout>
  );
}
