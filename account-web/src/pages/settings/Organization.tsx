import { useState, useEffect } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import InviteMemberModal from '@/components/org/InviteMemberModal';
import EditMemberModal from '@/components/org/EditMemberModal';
import EditRoleModal from '@/components/org/EditRoleModal';
import ConfirmDialog from '@/components/common/ConfirmDialog';
import { cn } from '@/lib/utils';
import { toast } from '@/lib/toast-event';
import { useAuth } from '@/context/AuthContext';
import api from '@/lib/api';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';

interface Member {
    id: number;
    firstName: string;
    lastName: string | null;
    userEmail: string; // From DTO
    roleName: string;
    roleId: number; // For editing
    designation: string | null;
    status: number; // 1: Active, 0: Pending
    createdTime: number; // UTC Milliseconds
    inviteToken?: string;
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

export default function Organization() {
    // ... (Existing state hooks remain same)
    const { currentOrg } = useAuth();
    const [activeTab, setActiveTab] = useState('General');
    const [members, setMembers] = useState<Member[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');

    // General Form State
    const [generalForm, setGeneralForm] = useState({
        orgName: '',
        website: '',
        description: '',
        employeeCount: ''
    });

    useEffect(() => {
        if (currentOrg) {
            setGeneralForm({
                orgName: currentOrg.org_name || '',
                website: currentOrg.website || '',
                description: currentOrg.description || '',
                employeeCount: currentOrg.employee_count || ''
            });
        }
    }, [currentOrg]);

    const handleGeneralChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        if (name === 'employeeCount') {
            // Only allow numbers
            if (value === '' || /^\d+$/.test(value)) {
                setGeneralForm(prev => ({ ...prev, [name]: value }));
            }
        } else {
            setGeneralForm(prev => ({ ...prev, [name]: value }));
        }
    };

    const handleGeneralSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        try {
            await api.put('/orgs', {
                org_id: currentOrg?.id,
                org_name: generalForm.orgName,
                website: generalForm.website,
                description: generalForm.description,
                employee_count: generalForm.employeeCount
            });
            toast.success('Organization updated successfully');
        } catch (error) {
            console.error("Update failed", error);
            // toast handled globally or add specific
        } finally {
            setIsLoading(false);
        }
    };

    // Action Menu State
    const [activeMenuId, setActiveMenuId] = useState<number | null>(null);
    const [editingMember, setEditingMember] = useState<Member | null>(null);

    const [roles, setRoles] = useState<any[]>([]);
    const [isRoleModalOpen, setIsRoleModalOpen] = useState(false);
    const [editingRole, setEditingRole] = useState<any | null>(null);

    // Confirmation Dialog State
    const [confirmState, setConfirmState] = useState<{
        isOpen: boolean;
        type: 'member' | 'role' | null;
        id: number | null;
        title: string;
        message: string;
    }>({
        isOpen: false,
        type: null,
        id: null,
        title: '',
        message: ''
    });

    useEffect(() => {
        // ... (useEffect Logic Remains Same, I will just copy previous logic if needed or ensure this block doesn't overwrite it incorrectly)
        // Wait, replace_file_content replaces a chunk. I need to be careful not to delete useEffect.
        // The instruction was 1-160. I need to preserve lines 54-63 (useEffect).

        if (currentOrg?.id) {
            if (activeTab === 'Members') {
                const timer = setTimeout(() => { fetchMembers(); }, 500);
                return () => clearTimeout(timer);
            } else if (activeTab === 'Roles') {
                fetchRoles();
            }
        }
    }, [currentOrg, searchQuery, activeTab]);

    // Close menu when clicking outside
    useEffect(() => {
        const handleClickOutside = () => setActiveMenuId(null);
        document.addEventListener('click', handleClickOutside);
        return () => document.removeEventListener('click', handleClickOutside);
    }, []);

    const fetchMembers = async () => {
        // ... (fetchMembers logic, re-using existing implementation to be safe)
        setIsLoading(true);
        try {
            let url = `/orgs/members?org_id=${currentOrg?.id}`;
            if (searchQuery) {
                url += `&search=${encodeURIComponent(searchQuery)}`;
            }
            const response = await api.get(url);
            if (response.data.success) {
                const mappedMembers = response.data.data.map((m: any, index: number) => ({
                    id: m.id,
                    firstName: m.first_name,
                    lastName: m.last_name,
                    userEmail: m.user_email,
                    roleName: m.role_name,
                    roleId: m.roleId || m.role_id,
                    designation: m.designation,
                    status: m.status,
                    createdTime: m.created_time,
                    inviteToken: m.invite_token || m.inviteToken,
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

    // Updated Handlers
    const confirmDeleteMember = (memberId: number) => {
        setConfirmState({
            isOpen: true,
            type: 'member',
            id: memberId,
            title: 'Remove Member',
            message: 'Are you sure you want to remove this member? They will lose access to the organization immediately.'
        });
    };

    const confirmDeleteRole = (roleId: number) => {
        setConfirmState({
            isOpen: true,
            type: 'role',
            id: roleId,
            title: 'Delete Role',
            message: 'Are you sure you want to delete this role? This action cannot be undone.'
        });
    };

    const handleExecuteDelete = async () => {
        if (!confirmState.id || !confirmState.type) return;

        setIsLoading(true);
        // Note: Global loading state might act weird on dialog, but OK for now. 
        // ideally Dialog has its own loading or we use a separate state. 
        // ConfirmDialog has `isLoading` prop. I should use a generic `isDeleting` state properly.
        // reusing `isLoading` for table might flicker table. I'll stick to it for simplicity or better use local var/await.

        try {
            if (confirmState.type === 'member') {
                await api.delete(`/orgs/member?org_id=${currentOrg?.id}&member_id=${confirmState.id}`);
                toast.success('Member removed successfully');
                fetchMembers();
            } else if (confirmState.type === 'role') {
                await api.delete(`/orgs/roles/${confirmState.id}?org_id=${currentOrg?.id}`);
                toast.success('Role deleted successfully');
                fetchRoles();
            }
        } catch (error) {
            console.error("Delete failed", error);
            // Error toast handled globally or we can add specific here
        } finally {
            setIsLoading(false);
            setConfirmState({ ...confirmState, isOpen: false });
        }
    };

    const fetchRoles = async () => {
        setIsLoading(true);
        try {
            const response = await api.get(`/orgs/roles?org_id=${currentOrg?.id}`);
            if (response.data.success) {
                const mappedRoles = response.data.data.map((r: any) => ({
                    id: r.id,
                    name: r.name,
                    description: r.description,
                    orgId: r.org_id,
                    createdTime: r.created_time
                }));
                setRoles(mappedRoles);
            }
        } catch (error) {
            console.error("Failed to fetch roles", error);
        } finally {
            setIsLoading(false);
        }
    };

    // Old handlers removed/replaced
    // handleDelete and handleDeleteRole are now confirmDeleteMember/Role in JSX usage.
    // I need to update the JSX calls too.

    return (
        <div className="w-full">


            <div className="p-8 max-w-6xl w-full mx-auto relative">


                <div className="mb-8">
                    <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Organization</h1>
                    {currentOrg && <p className="text-slate-500 text-sm mt-1">Manage <b>{currentOrg.org_name}</b> team and permissions.</p>}
                </div>

                {/* Tabs */}
                <div className="flex items-center gap-8 border-b border-border/60 mb-8">
                    {['General', 'Members', 'Roles'].map((tab) => (
                        <button
                            key={tab}
                            onClick={() => {
                                setActiveTab(tab);
                                setSearchQuery(''); // Clear search when switching tabs
                            }}
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

                {/* Content Switcher */}
                {activeTab === 'Members' ? (
                    <>
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
                                <div className="overflow-x-auto flex-1 min-h-[400px]">
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
                                                            <div className="flex flex-col max-w-[200px] min-w-[150px]">
                                                                <span className="text-sm font-semibold text-slate-900 truncate" title={`${member.firstName} ${member.lastName}`}>
                                                                    {member.firstName} {member.lastName}
                                                                </span>
                                                                <span className="text-xs text-slate-500 truncate" title={member.userEmail}>
                                                                    {member.userEmail}
                                                                </span>
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
                                                            {member.status === 1 ? (
                                                                <span className="text-sm text-slate-600 font-medium">Active</span>
                                                            ) : (
                                                                <div className="flex items-center gap-2">
                                                                    <span className="text-sm text-blue-600 font-semibold tracking-tight">Pending Invite</span>
                                                                    <button
                                                                        onClick={() => {
                                                                            const url = `${window.location.origin}/invitations?token=${member.inviteToken}`;
                                                                            navigator.clipboard.writeText(url);
                                                                            toast.success("Invitation link copied!");
                                                                        }}
                                                                        className="p-1 hover:bg-slate-100 rounded text-slate-400 hover:text-slate-600 transition-colors"
                                                                        title="Copy invite link"
                                                                    >
                                                                        <span className="material-symbols-outlined text-[16px]">content_copy</span>
                                                                    </button>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 text-right">
                                                        <div className="relative inline-block">
                                                            <button
                                                                onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    setActiveMenuId(activeMenuId === member.id ? null : member.id);
                                                                }}
                                                                className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
                                                            >
                                                                <span className="material-symbols-outlined text-[20px]">more_horiz</span>
                                                            </button>

                                                            {activeMenuId === member.id && (
                                                                <div className="absolute right-0 top-full mt-1 w-40 bg-white rounded-xl shadow-xl border border-slate-100 z-10 overflow-hidden animate-in fade-in zoom-in-95 duration-100 origin-top-right">
                                                                    <button
                                                                        className="w-full text-left px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50 flex items-center gap-2"
                                                                        onClick={(e) => {
                                                                            e.stopPropagation();
                                                                            setActiveMenuId(null);
                                                                            setEditingMember(member);
                                                                        }}
                                                                    >
                                                                        <span className="material-symbols-outlined text-lg">edit</span>
                                                                        Edit
                                                                    </button>
                                                                    <button
                                                                        className="w-full text-left px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 flex items-center gap-2"
                                                                        onClick={(e) => {
                                                                            e.stopPropagation();
                                                                            setActiveMenuId(null);
                                                                            confirmDeleteMember(member.id);
                                                                        }}
                                                                    >
                                                                        <span className="material-symbols-outlined text-lg">delete</span>
                                                                        Remove
                                                                    </button>
                                                                </div>
                                                            )}
                                                        </div>
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
                            {/* Pagination for Members */}
                            <div className="px-6 py-4 bg-slate-50/50 border-t border-slate-100 flex items-center justify-between mt-auto">
                                <span className="text-xs text-slate-500 font-medium">Showing {members.length} members</span>
                                {/* ...Pagination Buttons... */}
                            </div>
                        </div>
                    </>
                ) : activeTab === 'Roles' ? (
                    <>
                        {/* Roles Tab Content */}
                        <div className="flex items-center justify-between gap-4 mb-6">
                            <div className="relative w-80">
                                {/* Optional Search for Roles */}
                            </div>
                            <Button
                                className="rounded-xl shadow-md shadow-primary/20 gap-2 h-10 px-5 font-semibold"
                                onClick={() => setIsRoleModalOpen(true)}
                            >
                                <span className="material-symbols-outlined text-[20px]">add</span>
                                Create New Role
                            </Button>
                        </div>

                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden min-h-[400px]">
                            {isLoading ? (
                                <div className="flex items-center justify-center h-40 text-slate-400">Loading roles...</div>
                            ) : (
                                <table className="w-full text-left">
                                    <thead>
                                        <tr className="bg-slate-50/50 border-b border-slate-100">
                                            <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Role Name</th>
                                            <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Description</th>
                                            <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider">Created On</th>
                                            <th className="px-6 py-4 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-50">
                                        {roles.map((role) => (
                                            <tr key={role.id} className="hover:bg-slate-50/50 transition-colors">
                                                <td className="px-6 py-4 text-sm font-semibold text-slate-900">{role.name}</td>
                                                <td className="px-6 py-4 text-sm text-slate-600">{role.description}</td>
                                                <td className="px-6 py-4 text-sm text-slate-500">
                                                    {role.createdTime ? new Date(role.createdTime).toLocaleDateString() : '-'}
                                                </td>
                                                <td className="px-6 py-4 text-right">
                                                    {role.orgId ? (
                                                        <div className="flex items-center justify-end gap-2">
                                                            <button
                                                                onClick={() => setEditingRole(role)}
                                                                className="p-1.5 text-slate-400 hover:text-primary hover:bg-primary/10 rounded-lg transition-colors"
                                                                title="Edit Role"
                                                            >
                                                                <span className="material-symbols-outlined text-[20px]">edit</span>
                                                            </button>
                                                            <button
                                                                onClick={() => confirmDeleteRole(role.id)}
                                                                className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                                                title="Delete Role"
                                                            >
                                                                <span className="material-symbols-outlined text-[20px]">delete</span>
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        <span className="text-xs text-slate-400 italic">Read-only</span>
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                        {roles.length === 0 && (
                                            <tr><td colSpan={4} className="text-center py-8 text-slate-400">No roles found.</td></tr>
                                        )}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </>
                ) : (
                    <div className="w-full pb-12">
                        <form onSubmit={handleGeneralSubmit}>
                            <div className="bg-white border border-slate-200 rounded-xl shadow-sm">
                                <div className="p-8">
                                    <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-6">Organization Profile</h3>

                                    <div className="space-y-8">
                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                            <div className="space-y-2.5">
                                                <Label htmlFor="orgName" className="text-sm font-medium text-slate-700">Organization Name</Label>
                                                <Input
                                                    id="orgName"
                                                    name="orgName"
                                                    value={generalForm.orgName}
                                                    onChange={handleGeneralChange}
                                                    className="h-10 border-slate-200 focus-visible:ring-1 focus-visible:ring-blue-600 focus-visible:border-blue-600 focus-visible:ring-offset-0 bg-white"
                                                />
                                            </div>
                                            <div className="space-y-2.5">
                                                <Label htmlFor="employeeCount" className="text-sm font-medium text-slate-700">Employee Size</Label>
                                                <Input
                                                    id="employeeCount"
                                                    name="employeeCount"
                                                    value={generalForm.employeeCount}
                                                    onChange={handleGeneralChange}
                                                    className="h-10 border-slate-200 focus-visible:ring-1 focus-visible:ring-blue-600 focus-visible:border-blue-600 focus-visible:ring-offset-0 bg-white"
                                                />
                                            </div>
                                        </div>

                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                            <div className="space-y-2.5">
                                                <Label htmlFor="website" className="text-sm font-medium text-slate-700">Website URL</Label>
                                                <div className="relative">
                                                    <Input
                                                        id="website"
                                                        name="website"
                                                        value={generalForm.website}
                                                        onChange={handleGeneralChange}
                                                        className="h-10 pl-10 border-slate-200 focus:border-blue-600 focus:ring-blue-600/20 bg-white"
                                                        placeholder="https://"
                                                    />
                                                    <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">language</span>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="space-y-2.5">
                                            <Label htmlFor="description" className="text-sm font-medium text-slate-700">Description</Label>
                                            <div className="relative">
                                                <Textarea
                                                    id="description"
                                                    name="description"
                                                    value={generalForm.description}
                                                    onChange={handleGeneralChange}
                                                    className="min-h-[120px] border-slate-200 focus:border-blue-600 focus:ring-blue-600/20 bg-white resize-y shadow-sm"
                                                />
                                                <div className="absolute bottom-3 right-3 flex items-center gap-1 text-[10px] font-bold text-indigo-600 bg-indigo-50 px-2.5 py-1.5 rounded-md cursor-pointer hover:bg-indigo-100 transition-colors border border-indigo-100/50">
                                                    <span className="material-symbols-outlined text-[14px]">auto_awesome</span>
                                                    AI ANSWER
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div >

                                {/* Footer / Action Bar */}
                                < div className="flex items-center justify-between px-8 py-5 bg-slate-50/50 border-t border-slate-200 rounded-b-xl" >
                                    <p className="text-xs text-slate-500 font-medium">
                                        Last synced: <span className="text-slate-700">Just now</span>
                                    </p>
                                    <div className="flex items-center gap-4">
                                        <Button
                                            type="button"
                                            variant="ghost"
                                            className="text-slate-600 hover:text-slate-900 hover:bg-slate-200/50 font-medium"
                                            onClick={() => {/* Reset logic optional */ }}
                                        >
                                            Cancel
                                        </Button>
                                        <Button
                                            type="submit"
                                            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold h-10 px-6 rounded-lg shadow-sm shadow-blue-600/20"
                                            disabled={isLoading}
                                        >
                                            {isLoading ? 'Saving...' : 'Save Changes'}
                                        </Button>
                                    </div>
                                </div >
                            </div >
                        </form >
                    </div >
                )
                }

                {/* Modals & Dialogs */}
                <InviteMemberModal
                    isOpen={isInviteModalOpen}
                    onClose={() => setIsInviteModalOpen(false)}
                    onSuccess={fetchMembers}
                />

                <EditMemberModal
                    isOpen={!!editingMember}
                    onClose={() => setEditingMember(null)}
                    onSuccess={fetchMembers}
                    member={editingMember}
                    orgId={currentOrg?.id}
                />

                <EditRoleModal
                    isOpen={isRoleModalOpen || !!editingRole}
                    onClose={() => {
                        setIsRoleModalOpen(false);
                        setEditingRole(null);
                    }}
                    onSuccess={fetchRoles}
                    roleToEdit={editingRole}
                    orgId={currentOrg?.id}
                />

                <ConfirmDialog
                    isOpen={confirmState.isOpen}
                    onClose={() => setConfirmState({ ...confirmState, isOpen: false })}
                    onConfirm={handleExecuteDelete}
                    title={confirmState.title}
                    message={confirmState.message}
                    variant="danger"
                    confirmText="Delete"
                    isLoading={isLoading}
                />

            </div>
        </div>
    );
}
