import { useState, useEffect } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useAuth } from '@/context/AuthContext';
import api from '@/lib/api';

interface Member {
    id: number;
    firstName: string;
    lastName: string | null;
    userEmail: string; // From DTO
    roleName: string;
    designation: string | null;
    status: number; // 1: Active, 0: Pending
    avatar_initials?: string;
    avatar_color?: string;
}

const AVATAR_COLORS = [
    "bg-blue-100 text-blue-600",
    "bg-green-100 text-green-600",
    "bg-purple-100 text-purple-600",
    "bg-orange-100 text-orange-600",
    "bg-pink-100 text-pink-600"
];

const getInitials = (first: string, last: string | null) => {
    return (first.charAt(0) + (last ? last.charAt(0) : '')).toUpperCase();
};

import InviteMemberModal from '@/components/org/InviteMemberModal';

export default function Organization() {
    const { currentOrg } = useAuth();
    const [activeTab, setActiveTab] = useState('Members');
    const [members, setMembers] = useState<Member[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');

    useEffect(() => {
        if (currentOrg?.id) {
            const timer = setTimeout(() => {
                fetchMembers();
            }, 500);
            return () => clearTimeout(timer);
        }
    }, [currentOrg, searchQuery]);

    const fetchMembers = async () => {
        setIsLoading(true);
        try {
            let url = `/orgs/members?org_id=${currentOrg?.id}`;
            if (searchQuery) {
                url += `&search=${encodeURIComponent(searchQuery)}`;
            }
            const response = await api.get(url);
            if (response.data.success) {
                // Transform data for UI (add colors)
                // Backend returns snake_case due to application.properties setting
                const mappedMembers = response.data.data.map((m: any, index: number) => ({
                    id: m.id,
                    firstName: m.first_name,
                    lastName: m.last_name,
                    userEmail: m.user_email,
                    roleName: m.role_name,
                    designation: m.designation,
                    status: m.status,
                    createdTime: m.created_time,
                    avatar_initials: getInitials(m.first_name || '', m.last_name),
                    avatar_color: AVATAR_COLORS[index % AVATAR_COLORS.length]
                }));
                setMembers(mappedMembers);
            }
        } catch (error) {
            console.error("Failed to fetch members", error);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="p-8 max-w-6xl w-full mx-auto relative">
            <InviteMemberModal
                isOpen={isInviteModalOpen}
                onClose={() => setIsInviteModalOpen(false)}
                onInviteSuccess={fetchMembers}
            />

            <div className="mb-8">
                <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Organization</h1>
                {currentOrg && <p className="text-slate-500 text-sm mt-1">Manage <b>{currentOrg.org_name}</b> team and permissions.</p>}
            </div>

            {/* Tabs */}
            <div className="flex items-center gap-8 border-b border-border/60 mb-8">
                {['General', 'Members', 'Roles'].map((tab) => (
                    <button
                        key={tab}
                        onClick={() => setActiveTab(tab)}
                        className={cn(
                            "pb-4 text-sm font-medium relative transition-colors",
                            activeTab === tab
                                ? "text-primary font-semibold"
                                : "text-slate-500 hover:text-slate-700"
                        )}
                    >
                        {tab}
                        {activeTab === tab && (
                            <span className="absolute bottom-0 left-0 w-full h-[2px] bg-primary rounded-t-full"></span>
                        )}
                    </button>
                ))}
            </div>

            {/* Actions Bar */}
            <div className="flex items-center justify-between gap-4 mb-6">
                <div className="relative w-80">
                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">search</span>
                    <Input
                        placeholder="Search members..."
                        className="pl-10 h-10 bg-white shadow-sm border-slate-200 focus-visible:ring-primary/20 focus-visible:border-primary rounded-xl"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
                <Button
                    className="rounded-xl shadow-md shadow-primary/20 gap-2 h-10 px-5 font-semibold"
                    onClick={() => setIsInviteModalOpen(true)}
                >
                    <span className="material-symbols-outlined text-[20px]">add</span>
                    Invite People
                </Button>
            </div>

            {/* Members Table */}
            <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden min-h-[400px] flex flex-col justify-between">
                {isLoading ? (
                    <div className="flex items-center justify-center flex-1 text-slate-400">Loading members...</div>
                ) : (
                    <div className="overflow-x-auto flex-1">
                        <table className="w-full text-left">
                            <thead>
                                <tr className="bg-slate-50/50 border-b border-slate-100">
                                    <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">User</th>
                                    <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Designation</th>
                                    <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Role</th>
                                    <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Joined</th>
                                    <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Status</th>
                                    <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-50">
                                {members.map((member) => (
                                    <tr key={member.id} className="hover:bg-slate-50/50 transition-colors group">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className={cn("w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm", member.avatar_color)}>
                                                    {member.avatar_initials}
                                                </div>
                                                <div className="flex flex-col">
                                                    <span className="text-sm font-semibold text-slate-900">{member.firstName} {member.lastName}</span>
                                                    <span className="text-xs text-slate-500">{member.userEmail}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-600">
                                            {member.designation || '-'}
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-slate-100 text-slate-700 text-xs font-medium cursor-pointer hover:bg-slate-200 transition-colors">
                                                {member.roleName}
                                                <span className="material-symbols-outlined text-sm leading-none">expand_more</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm text-slate-500">
                                            {new Date(member.createdTime).toLocaleDateString()}
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2">
                                                <span className={cn("h-1.5 w-1.5 rounded-full", member.status === 1 ? "bg-emerald-500" : "bg-amber-500")}></span>
                                                <span className="text-sm text-slate-600 font-medium">{member.status === 1 ? 'Active' : 'Pending'}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <button className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                                                <span className="material-symbols-outlined text-[20px]">more_horiz</span>
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {members.length === 0 && (
                                    <tr><td colSpan={6} className="text-center py-8 text-slate-400">No members found.</td></tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* Pagination */}
                <div className="px-6 py-4 bg-slate-50/50 border-t border-slate-100 flex items-center justify-between mt-auto">
                    <span className="text-xs text-slate-500 font-medium">Showing {members.length} members</span>
                    <div className="flex items-center gap-1">
                        <button className="p-1.5 text-slate-400 hover:text-slate-600 disabled:opacity-30" disabled>
                            <span className="material-symbols-outlined text-[20px]">chevron_left</span>
                        </button>
                        <button className="px-3 py-1 text-xs font-bold rounded-lg bg-white border border-slate-200 text-primary shadow-sm">1</button>
                        <button className="px-3 py-1 text-xs font-bold rounded-lg hover:bg-white hover:border-slate-200 text-slate-500 transition-all">2</button>
                        <button className="px-3 py-1 text-xs font-bold rounded-lg hover:bg-white hover:border-slate-200 text-slate-500 transition-all">3</button>
                        <button className="p-1.5 text-slate-400 hover:text-slate-600">
                            <span className="material-symbols-outlined text-[20px]">chevron_right</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
