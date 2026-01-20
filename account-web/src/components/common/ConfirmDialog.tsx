import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';

interface ConfirmDialogProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
    variant?: 'danger' | 'primary';
    isLoading?: boolean;
}

export default function ConfirmDialog({
    isOpen,
    onClose,
    onConfirm,
    title,
    message,
    confirmText = 'Confirm',
    cancelText = 'Cancel',
    variant = 'danger',
    isLoading = false,
}: ConfirmDialogProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <Card className="w-full max-w-md shadow-2xl relative animate-in zoom-in-95 duration-200 border-0">
                <CardHeader className="pb-2">
                    <CardTitle className="text-xl font-bold text-slate-900">{title}</CardTitle>
                </CardHeader>
                <CardContent className="pb-6">
                    <p className="text-slate-600 text-sm leading-relaxed">
                        {message}
                    </p>
                </CardContent>
                <CardFooter className="flex items-center justify-end gap-3 bg-slate-50/50 p-4 rounded-b-xl border-t border-slate-100">
                    <Button
                        variant="ghost"
                        onClick={onClose}
                        className="font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-200/50"
                        disabled={isLoading}
                    >
                        {cancelText}
                    </Button>
                    <Button
                        onClick={onConfirm}
                        className={`font-semibold shadow-lg transition-all ${variant === 'danger'
                                ? 'bg-red-600 hover:bg-red-700 text-white shadow-red-500/20'
                                : 'bg-slate-900 hover:bg-slate-800 text-white shadow-slate-900/20'
                            }`}
                        disabled={isLoading}
                    >
                        {isLoading ? 'Processing...' : confirmText}
                    </Button>
                </CardFooter>
            </Card>
        </div>
    );
}
