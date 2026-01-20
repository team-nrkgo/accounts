import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { cn } from '@/lib/utils';
import { useNavigate } from 'react-router-dom';

interface SidebarItemProps {
    icon: string;
    label: string;
    href: string;
    active?: boolean;
}

function SidebarItem({ icon, label, href, active }: SidebarItemProps) {
    return (
        <Link
            to={href}
            className={cn(
                "flex items-center gap-3 px-4 py-2 rounded-md transition-all text-sm font-medium",
                active
                    ? "bg-white/10 text-white"
                    : "hover:bg-white/5 hover:text-white text-slate-400"
            )}
        >
            <span className="material-symbols-outlined text-[20px]">{icon}</span>
            <span>{label}</span>
        </Link>
    );
}

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
    const { user, logout } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();

    const isActive = (path: string) => location.pathname === path;

    return (
        <div className="flex min-h-screen font-sans bg-slate-50 text-slate-900">
            {/* Sidebar */}
            <aside className="w-64 bg-sidebar-bg text-slate-400 flex flex-col fixed inset-y-0 z-30">
                <div className="p-8 flex items-center gap-3">
                    <div className="size-8 rounded bg-primary flex items-center justify-center">
                        <span className="material-symbols-outlined text-white text-xl">bolt</span>
                    </div>
                    <span className="text-white font-semibold text-sm tracking-tight">Acme Console</span>
                </div>

                <nav className="flex-1 px-4 space-y-1">
                    <p className="text-[11px] uppercase tracking-widest font-bold text-slate-400 mb-4 px-4 mt-6">PRODUCTS</p>
                    {/* <SidebarItem icon="dashboard" label="Dashboard" href="/" active={isActive('/')} /> */}
                    <SidebarItem icon="apps" label="My Apps" href="/apps" active={isActive('/apps')} />

                    <p className="text-[11px] uppercase tracking-widest font-bold text-slate-400 mb-4 px-4 mt-8">Account</p>
                    <SidebarItem icon="person" label="Profile" href="/settings/profile" active={isActive('/settings/profile')} />
                    <SidebarItem icon="corporate_fare" label="Organization" href="/settings/organization" active={isActive('/settings/organization')} />
                    <SidebarItem icon="security" label="Security" href="/settings/security" active={isActive('/settings/security')} />
                    {/* <SidebarItem icon="credit_card" label="Billing" href="/settings/billing" active={isActive('/settings/billing')} /> */}
                </nav>

                <div className="p-6 border-t border-white/5 mt-auto">
                    <button
                        onClick={logout}
                        className="flex w-full items-center gap-3 px-4 py-2 text-sm font-medium hover:text-red-400 transition-colors"
                    >
                        <span className="material-symbols-outlined text-[20px]">logout</span>
                        <span>Sign out</span>
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 ml-64 flex flex-col min-h-screen">
                {/* Sticky Header */}
                <header className="h-16 flex items-center justify-between px-10 sticky top-0 bg-slate-50/80 backdrop-blur-md z-20">
                    {/* <div className="flex items-center gap-2 text-[13px] text-slate-500">
                        <span>Settings</span>
                        <span className="material-symbols-outlined text-sm">chevron_right</span>
                        <span className="text-slate-900 font-medium">Profile Settings</span>
                    </div> */}
                    <div></div>

                    <div className="flex items-center gap-6">
                        {/* <button className="text-slate-400 hover:text-slate-900 transition-colors">
                            <span className="material-symbols-outlined">search</span>
                        </button>
                        <button className="text-slate-400 hover:text-slate-900 transition-colors relative">
                            <span className="material-symbols-outlined">notifications</span>
                            <span className="absolute -top-1 -right-1 size-2 bg-primary rounded-full ring-2 ring-slate-50"></span>
                        </button> */}
                        <div className="size-8 rounded-full bg-slate-200 overflow-hidden ring-1 ring-slate-200">
                            <div className="w-full h-full bg-slate-300 flex items-center justify-center text-slate-500 font-bold">
                                {user?.first_name?.charAt(0) || 'U'}
                            </div>
                        </div>
                    </div>
                </header>

                {/* Page Content */}
                {children}

            </main>
        </div>
    );
}
