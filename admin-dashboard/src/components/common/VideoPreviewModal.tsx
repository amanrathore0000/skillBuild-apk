import React from 'react';
import { UploadedVideo } from '../../types/admin';
import { X, Play, AlertTriangle, CheckCircle, Trash2, Eye, ThumbsUp } from 'lucide-react';

interface VideoPreviewModalProps {
  video: UploadedVideo | null;
  isOpen: boolean;
  onClose: () => void;
  onFlag: (video: UploadedVideo) => void;
  onRemove: (video: UploadedVideo) => void;
  onApprove: (video: UploadedVideo) => void;
}

export const VideoPreviewModal: React.FC<VideoPreviewModalProps> = ({
  video,
  isOpen,
  onClose,
  onFlag,
  onRemove,
  onApprove
}) => {
  if (!isOpen || !video) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-container" style={{ maxWidth: '720px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '32px',
              height: '32px',
              borderRadius: '8px',
              backgroundColor: 'rgba(99, 102, 241, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--primary)'
            }}>
              <Play size={18} />
            </div>
            <div>
              <h3 style={{ fontSize: '16px', fontWeight: 800 }}>Admin Video Inspection Player</h3>
              <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Review uploaded curriculum media for bad practice</p>
            </div>
          </div>
          <button onClick={onClose} className="btn btn-outline" style={{ padding: '6px', borderRadius: '50%' }}>
            <X size={16} />
          </button>
        </div>

        <div className="modal-body" style={{ padding: 0 }}>
          {/* HTML5 Video Player */}
          <div style={{ position: 'relative', width: '100%', aspectRatio: '16/9', backgroundColor: '#000' }}>
            <video
              src={video.videoUrl}
              poster={video.thumbnailUrl}
              controls
              autoPlay
              style={{ width: '100%', height: '100%', objectFit: 'contain' }}
            />
          </div>

          <div style={{ padding: '20px 24px' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '14px' }}>
              <div>
                <span className={`badge badge-${video.status === 'APPROVED' ? 'active' : video.status === 'FLAGGED' ? 'warned' : 'blocked'}`}>
                  {video.status}
                </span>
                <h2 style={{ fontSize: '18px', fontWeight: 800, marginTop: '8px', color: 'var(--text-primary)' }}>
                  {video.title}
                </h2>
                <p style={{ fontSize: '13px', color: 'var(--primary-light)', marginTop: '2px' }}>
                  Course: {video.courseTitle}
                </p>
              </div>

              <div style={{ textAlign: 'right' }}>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Uploaded on</span>
                <p style={{ fontSize: '13px', fontWeight: 700 }}>{video.uploadDate}</p>
              </div>
            </div>

            {/* Bad practice warning callout if flagged */}
            {video.flaggedForBadPractice && (
              <div style={{
                marginTop: '16px',
                padding: '12px 14px',
                borderRadius: '8px',
                background: 'rgba(239, 68, 68, 0.1)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '10px'
              }}>
                <AlertTriangle size={18} color="var(--danger)" style={{ marginTop: '2px', flexShrink: 0 }} />
                <div>
                  <strong style={{ fontSize: '13px', color: 'var(--danger)' }}>Flagged for Bad Practice:</strong>
                  <p style={{ fontSize: '12px', color: '#fca5a5', marginTop: '2px' }}>
                    {video.flagReason || 'Reported by community users for policy violation or pirated media.'}
                  </p>
                </div>
              </div>
            )}

            {/* Stats Row */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '18px',
              marginTop: '16px',
              padding: '12px 0',
              borderTop: '1px solid var(--border-subtle)',
              borderBottom: '1px solid var(--border-subtle)',
              fontSize: '13px',
              color: 'var(--text-secondary)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <img
                  src={video.mentorAvatar}
                  alt={video.mentorName}
                  style={{ width: '22px', height: '22px', borderRadius: '50%' }}
                />
                <span>By <strong>{video.mentorName}</strong></span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Eye size={15} />
                <span>{video.views.toLocaleString()} Views</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <ThumbsUp size={15} />
                <span>{video.likes} Likes</span>
              </div>
              <div>
                <span>Duration: <strong>{video.duration}</strong></span>
              </div>
            </div>
          </div>
        </div>

        {/* Footer Moderation Actions */}
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={onClose}>
            Close
          </button>
          {video.status !== 'APPROVED' && (
            <button className="btn btn-success" onClick={() => { onApprove(video); onClose(); }}>
              <CheckCircle size={15} />
              Approve Video
            </button>
          )}
          {video.status !== 'FLAGGED' && (
            <button className="btn btn-warning" onClick={() => { onFlag(video); onClose(); }}>
              <AlertTriangle size={15} />
              Flag Bad Practice
            </button>
          )}
          <button className="btn btn-danger" onClick={() => { onRemove(video); onClose(); }}>
            <Trash2 size={15} />
            Remove from Server
          </button>
        </div>
      </div>
    </div>
  );
};
