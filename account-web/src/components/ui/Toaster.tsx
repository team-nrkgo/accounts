import { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { toast, ToastEventDetail } from '@/lib/toast-event';
import { cn } from '@/lib/utils'; // Assuming this exists as per checked package.json/updates

export function Toaster() {
    const [toasts, setToasts] = useState<ToastEventDetail[]>([]);

    useEffect(() => {
        const handleToast = (event: Event) => {
            const customEvent = event as CustomEvent<ToastEventDetail>;
            setToasts((prev) => [...prev, customEvent.detail]);

            // Auto dismiss
            setTimeout(() => {
                setToasts((prev) => prev.filter((t) => t.id !== customEvent.detail.id));
            }, 3000); // 3 seconds
        };

        toast.addEventListener('toast', handleToast);
        return () => toast.removeEventListener('toast', handleToast);
    }, []);

    return (
        <div className="fixed top-4 right-4 z-[9999] flex flex-col gap-2 pointer-events-none">
            <AnimatePresence mode='popLayout'>
                {toasts.map((t) => (
                    <motion.div
                        key={t.id}
                        layout
                        initial={{ opacity: 0, y: -20, scale: 0.95 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.9, transition: { duration: 0.2 } }}
                        className={cn(
                            "pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg border text-sm font-medium min-w-[300px]",
                            t.type === 'error' && "bg-red-50 border-red-200 text-red-600",
                            t.type === 'success' && "bg-emerald-50 border-emerald-200 text-emerald-600",
                            t.type === 'info' && "bg-white border-slate-200 text-slate-700"
                        )}
                    >
                        <span className="material-symbols-outlined text-[20px]">
                            {t.type === 'error' ? 'error' : t.type === 'success' ? 'check_circle' : 'info'}
                        </span>
                        {t.message}
                    </motion.div>
                ))}
            </AnimatePresence>
        </div>
    );
}
