interface AppCardProps {
    name: string;
    category: string;
    description: string;
    href: string;
    logo: string;
    icon?: string;
}

function AppCard({ name, category, description, href, logo, icon }: AppCardProps) {
    return (
        <a
            href={href}
            target="_blank"
            rel="noopener noreferrer"
            className="group relative bg-white rounded-2xl border border-slate-200 p-6 hover:shadow-xl hover:border-slate-300 transition-all duration-300 flex flex-col h-full"
        >
            <div className="flex items-start justify-between mb-6">
                <div className="size-12 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-center overflow-hidden">
                    {logo ? (
                        <img src={logo} alt={name} className="w-8 h-8 object-contain" />
                    ) : (
                        <span className="material-symbols-outlined text-slate-400 text-2xl">{icon}</span>
                    )}
                </div>
                <span className="material-symbols-outlined text-slate-300 group-hover:text-blue-500 transition-colors text-xl">open_in_new</span>
            </div>

            <div className="mb-4">
                <h3 className="font-bold text-slate-900 group-hover:text-blue-600 transition-colors">{name}</h3>
                <p className="text-[10px] font-bold text-blue-600 uppercase tracking-widest mt-1">{category}</p>
            </div>

            <p className="text-slate-500 text-sm leading-relaxed mb-6 flex-1">
                {description}
            </p>

            <button className="w-full py-2 px-4 bg-blue-600 text-white font-semibold text-xs rounded-lg hover:bg-blue-700 transition-colors">
                Open Application
            </button>
        </a>
    );
}

export default function Apps() {
    return (
        <div className="w-full">
            <div className="px-10 py-10 max-w-6xl mx-auto space-y-10">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">All Applications</h1>
                    <p className="text-slate-500 mt-2 text-sm">Central hub for all your NRKGo workspace applications.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    <AppCard
                        name="SnapSteps"
                        category="Productivity"
                        description="Automatically document your workflows and create step-by-step guides in seconds."
                        href="https://snapsteps.nrkgo.com/"
                        logo="https://snapsteps.nrkgo.com/assets/logo.png"
                    />
                </div>
            </div>
        </div>
    );
}
