import { useAuth } from '@/context/AuthContext';
import { useState } from 'react';
import api from '@/lib/api';

export default function Profile() {
    const { user, refreshUser } = useAuth();
    const [isSaving, setIsSaving] = useState(false);
    const [formData, setFormData] = useState({
        first_name: user?.first_name || '',
        last_name: user?.last_name || '',
        mobile_number: user?.mobile_number || '', // Assuming context user has this
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSave = async () => {
        setIsSaving(true);
        try {
            await api.put('/user', formData); // Correct endpoint based on UserController using PUT
            await refreshUser(); // Refresh context to show new name in header
            alert("Profile updated successfully!"); // SImple feedback for now
        } catch (e) {
            console.error("Failed to save", e);
            alert("Failed to update profile. Please try again.");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <>
            <div className="px-10 py-8 pb-32 w-full space-y-12">

                <div className="max-w-4xl">
                    <h1 className="text-3xl font-bold tracking-tight text-slate-900">Account Settings</h1>
                    <p className="text-slate-500 mt-2">Manage your public profile and account preferences.</p>
                </div>

                {/* Public Profile Section */}
                <div className="max-w-4xl">
                    <p className="text-[11px] uppercase tracking-widest font-bold text-slate-400 mb-4">Public Profile</p>
                    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
                        <div className="flex items-center gap-10">
                            <div className="relative group">
                                <div className="size-24 rounded-full overflow-hidden border border-gray-100 shadow-inner bg-slate-100 flex items-center justify-center">
                                    {/* Placeholder Avatar */}
                                    <span className="text-3xl font-bold text-slate-300">
                                        {user?.first_name?.charAt(0)}
                                    </span>
                                </div>
                                <button className="absolute -bottom-1 -right-1 size-8 bg-white border border-gray-200 rounded-full shadow-sm flex items-center justify-center text-slate-600 hover:text-primary transition-colors">
                                    <span className="material-symbols-outlined text-lg">photo_camera</span>
                                </button>
                            </div>
                            <div className="flex-1 grid grid-cols-2 gap-8">
                                <div className="space-y-2">
                                    <label className="text-[13px] font-semibold text-slate-700">First Name</label>
                                    <input
                                        name="first_name"
                                        value={formData.first_name}
                                        onChange={handleChange}
                                        className="w-full h-11 px-4 text-sm rounded-lg border-gray-200 bg-white focus:ring-2 focus:ring-primary focus:border-primary transition-all shadow-sm outline-none border"
                                        type="text"
                                    />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-[13px] font-semibold text-slate-700">Last Name</label>
                                    <input
                                        name="last_name"
                                        value={formData.last_name}
                                        onChange={handleChange}
                                        className="w-full h-11 px-4 text-sm rounded-lg border-gray-200 bg-white focus:ring-2 focus:ring-primary focus:border-primary transition-all shadow-sm outline-none border"
                                        type="text"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Contact Info Section */}
                <div className="max-w-4xl">
                    <p className="text-[11px] uppercase tracking-widest font-bold text-slate-400 mb-4">Contact Information</p>
                    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8 space-y-8">
                        <div className="grid grid-cols-2 gap-8">
                            <div className="space-y-2">
                                <label className="text-[13px] font-semibold text-slate-700">Email Address</label>
                                <div className="relative">
                                    <input
                                        className="w-full h-11 pl-4 pr-10 text-sm rounded-lg border-gray-200 bg-slate-50 text-slate-500 cursor-not-allowed italic border outline-none"
                                        readOnly
                                        type="email"
                                        value={user?.email || ''}
                                    />
                                    <span className="material-symbols-outlined absolute right-3 top-2.5 text-slate-300 text-sm">lock</span>
                                </div>
                                <p className="text-[12px] text-slate-400">Verified official email address.</p>
                            </div>
                            <div className="space-y-2">
                                <label className="text-[13px] font-semibold text-slate-700">Mobile Number</label>
                                <input
                                    name="mobile_number"
                                    value={formData.mobile_number}
                                    onChange={handleChange}
                                    className="w-full h-11 px-4 text-sm rounded-lg border-gray-200 bg-white focus:ring-2 focus:ring-primary focus:border-primary transition-all shadow-sm outline-none border"
                                    type="tel"
                                />
                            </div>
                        </div>
                    </div>
                </div>

                {/* Danger Zone */}
                <div className="max-w-4xl">
                    <p className="text-[11px] uppercase tracking-widest font-bold text-red-500 mb-4">Danger Zone</p>
                    <div className="bg-red-50 border border-red-200 rounded-xl p-8 flex items-center justify-between">
                        <div className="max-w-lg">
                            <h4 className="font-bold text-red-900 mb-1">Delete Account</h4>
                            <p className="text-sm text-red-700/80 leading-relaxed">
                                Once you delete your account, there is no going back. Please be certain. All data, files and history will be permanently erased.
                            </p>
                        </div>
                        <button className="px-6 py-2.5 bg-red-600 hover:bg-red-700 text-white text-sm font-semibold rounded-lg shadow-sm transition-all whitespace-nowrap">
                            Delete Account
                        </button>
                    </div>
                </div>
            </div>

            {/* Floating Footer */}
            <footer className="fixed bottom-0 left-64 right-0 px-10 py-5 bg-white/80 backdrop-blur-xl border-t border-gray-200 flex items-center justify-between z-40">
                <div className="text-[12px] text-slate-400 font-medium">
                    Last synced: Just now
                </div>
                <div className="flex gap-3">
                    <button className="px-5 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                        Cancel
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={isSaving}
                        className="px-8 py-2.5 text-sm font-semibold text-white bg-primary hover:bg-blue-600 rounded-lg shadow-lg shadow-primary/20 transition-all disabled:opacity-70"
                    >
                        {isSaving ? 'Saving...' : 'Save Changes'}
                    </button>
                </div>
            </footer>
        </>
    );
}
