
export default function Apps() {
    return (
        <div className="px-10 py-8 w-full">
            <div className="max-w-6xl mx-auto space-y-8">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">My Apps</h1>
                    <p className="text-slate-500 mt-2 text-sm">Access and manage your subscribed applications.</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {/* SnapSteps Card */}
                    <a
                        href="https://snapsteps.nrkgo.com/"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="group relative bg-white rounded-xl border border-slate-200 overflow-hidden hover:shadow-lg hover:border-slate-300 transition-all duration-300 flex flex-col h-full"
                    >
                        {/* Card Image Placeholder / Gradient */}
                        <div className="h-40 bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center p-6">
                            <div className="bg-white/20 p-4 rounded-xl backdrop-blur-sm">
                                <span className="material-symbols-outlined text-white text-5xl">screenshot_monitor</span>
                            </div>
                        </div>

                        <div className="p-6 flex-1 flex flex-col">
                            <div className="flex items-start justify-between mb-4">
                                <div>
                                    <h3 className="font-bold text-lg text-slate-900 group-hover:text-blue-600 transition-colors">SnapSteps</h3>
                                    <p className="text-xs font-semibold text-blue-600 uppercase tracking-wider mt-1">Productivity</p>
                                </div>
                                <span className="material-symbols-outlined text-slate-300 group-hover:text-blue-500 transition-colors">open_in_new</span>
                            </div>

                            <p className="text-slate-500 text-sm leading-relaxed mb-6 flex-1">
                                Automatically document your workflows and create step-by-step guides in seconds.
                            </p>

                            <div className="flex items-center gap-2 mt-auto">
                                <button className="w-full py-2.5 px-4 bg-slate-50 hover:bg-slate-100 text-slate-700 font-medium text-sm rounded-lg border border-slate-200 transition-colors flex items-center justify-center gap-2">
                                    Launch App
                                </button>
                            </div>
                        </div>
                    </a>
                </div>
            </div>
        </div>
    );
}
