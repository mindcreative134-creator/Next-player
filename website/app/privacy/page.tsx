import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Privacy Policy - mpvExtended Pro',
  description: 'Privacy Policy for mpvExtended Pro media player application.',
};

export default function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen bg-neutral-900 text-neutral-100 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto bg-neutral-800 border border-neutral-700 rounded-2xl p-8 shadow-xl">
        <h1 className="text-3xl font-bold text-center text-white mb-2">Privacy Policy</h1>
        <p className="text-center text-sm text-neutral-400 mb-8">Effective Date: February 1, 2026</p>

        <div className="space-y-6 text-neutral-300 leading-relaxed">
          <p>
            <strong className="text-white">Creative Mind Studio</strong> (&quot;we&quot;, &quot;us&quot;, or &quot;our&quot;) operates the{' '}
            <strong className="text-white">mpvExtended Pro</strong> mobile application (the &quot;Service&quot;, package name: <code className="bg-neutral-950 px-2 py-1 rounded text-neutral-200">com.nextplayer.pro</code>). This page informs you of our policies regarding the collection, use, and disclosure of personal data when you use our Service and the choices you have associated with that data.
          </p>

          <h2 className="text-xl font-semibold text-white pt-4 border-t border-neutral-700">1. Information Collection and Use</h2>
          <p>We do not collect any personally identifiable information from your device. The Service is designed to function primarily as a local media player for playing video and audio files.</p>

          <h3 className="text-lg font-medium text-white">Types of Data Processed</h3>
          <ul className="list-disc pl-6 space-y-2">
            <li><strong className="text-neutral-200">Local Media Files:</strong> The Service requires access to your device&apos;s media storage to discover, list, and play video, audio, and subtitle files. All media processing occurs locally on your device. Your local media files and private data are never collected, stored, or transmitted to external servers.</li>
            <li><strong className="text-neutral-200">Usage Data:</strong> We may collect non-personally identifiable information on how the Service is accessed and used. This Usage Data may include information such as your device&apos;s Internet Protocol address (IP address), device type, operating system version, and app usage statistics.</li>
          </ul>

          <h2 className="text-xl font-semibold text-white pt-4 border-t border-neutral-700">2. Third-Party Services</h2>
          <p>We use third-party services that may collect information used to identify your device for advertisement and analytics purposes.</p>
          <ul className="list-disc pl-6 space-y-2">
            <li><strong className="text-neutral-200">Google AdMob:</strong> Our Service uses Google AdMob to display advertisements. AdMob may collect and use data such as your device&apos;s Advertising ID to deliver relevant ads and prevent fraud. For more information, please visit{' '}
              <a href="https://policies.google.com/technologies/partner-sites" target="_blank" rel="noopener noreferrer" className="text-blue-400 hover:underline">
                How Google uses data when you use our partners&apos; sites or apps
              </a>.
            </li>
          </ul>

          <h2 className="text-xl font-semibold text-white pt-4 border-t border-neutral-700">3. Permissions</h2>
          <p><strong className="text-white">mpvExtended Pro</strong> requests the following Android permissions strictly to deliver its core functionality as a media player:</p>
          <ul className="list-disc pl-6 space-y-2">
            <li><strong className="text-neutral-200">Media & Storage Access (READ_MEDIA_VIDEO, READ_MEDIA_AUDIO, READ_EXTERNAL_STORAGE):</strong> Required to discover, browse, and play local video, audio, and subtitle files stored on your device.</li>
            <li><strong className="text-neutral-200">Internet Access & Network State:</strong> Required to load advertisements (via AdMob) and handle network streaming (e.g., SMB, FTP, WebDAV, HLS) initiated by the user.</li>
            <li><strong className="text-neutral-200">Advertising ID (com.google.android.gms.permission.AD_ID):</strong> Used by Google AdMob to provide appropriate ad content.</li>
            <li><strong className="text-neutral-200">Foreground Service (FOREGROUND_SERVICE_MEDIA_PLAYBACK):</strong> Allows continuous audio and video playback when the app runs in background mode.</li>
          </ul>

          <h2 className="text-xl font-semibold text-white pt-4 border-t border-neutral-700">4. Children&apos;s Privacy</h2>
          <p>Our Service does not address anyone under the age of 13. We do not knowingly collect personally identifiable information from children under 13 years of age.</p>

          <h2 className="text-xl font-semibold text-white pt-4 border-t border-neutral-700">5. Changes to This Privacy Policy</h2>
          <p>We may update our Privacy Policy from time to time. You are advised to review this Privacy Policy periodically for any changes. Changes to this Privacy Policy are effective when they are posted on this page.</p>

          <h2 className="text-xl font-semibold text-white pt-4 border-t border-neutral-700">6. Contact Us</h2>
          <p>If you have any questions or suggestions regarding this Privacy Policy, please contact us at:</p>
          <p><strong className="text-white">Email:</strong> <a href="mailto:mindcreative134@gmail.com" className="text-blue-400 hover:underline">mindcreative134@gmail.com</a></p>

          <div className="footer pt-6 border-t border-neutral-700 text-sm text-neutral-400 text-center">
            <p>Created for <strong className="text-neutral-300">mpvExtended Pro</strong> (<code className="bg-neutral-950 px-2 py-0.5 rounded text-neutral-300">com.nextplayer.pro</code>) — Developed by <strong className="text-neutral-300">Creative Mind Studio</strong>.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
