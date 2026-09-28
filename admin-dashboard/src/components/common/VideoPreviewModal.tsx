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
      <div className="modal-container" style={{ maxWidth: '740px', backgroundColor: '#ffffff' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '36px',
              height: '36px',
              borderRadius: '10px',
              backgroundColor: '#ecfdf5',
              border: '1px solid #a7f3d0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#10b981'
            }}>
              <Play size={18} />
            </div>
            <div>
              <h3 style={{ fontSize: '16px', fontWeight: 800, color: '#09090b' }}>Admin Video Inspection Player</h3>
              <p style={{ fontSize: '11px', color: '#64748b', fontWeight: 500 }}>Review uploaded curriculum media for bad practice</p>
            </div>
          </div>
          <button onClick={onClose} className="btn btn-outline" style={{ padding: '6px', borderRadius: '50%' }}>
            <X size={16} />
          </button>
        </div>

        <div className="modal-body" style={{ padding: 0, backgroundColor: '#ffffff' }}>
          {/* HTML5 Video Player */}
          <div style={{ position: 'relative', width: '100%', aspectRatio: '16/9', backgroundColor: '#09090b' }}>
            <video
              src={video.videoUrl}
              poster={video.thumbnailUrl}
              controls
              autoPlay
              style={{ width: '100%', height: '100%', objectFit: 'contain' }}
            />
          </div>

          <div style={{ padding: '20px 24px', backgroundColor: '#ffffff' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '14px' }}>
              <div>
                <span className={`badge badge-${video.status === 'APPROVED' ? 'active' : 'danger'}`}>
                  {video.status}
                </span>
                <h2 style={{ fontSize: '18px', fontWeight: 800, marginTop: '8px', color: '#09090b' }}>
                  {video.title}
                </h2>
                <p style={{ fontSize: '13px', color: '#10b981', marginTop: '2px', fontWeight: 700 }}>
                  Course: {video.courseTitle}
                </p>
              </div>

              <div style={{ textAlign: 'right' }}>
                <span style={{ fontSize: '11px', color: '#64748b', fontWeight: 600 }}>Uploaded on</span>
                <p style={{ fontSize: '13px', fontWeight: 700, color: '#09090b' }}>{video.uploadDate}</p>
              </div>
            </div>

            {/* Bad practice warning callout if flagged */}
            {video.flaggedForBadPractice && (
              <div style={{
                marginTop: '16px',
                padding: '12px 16px',
                borderRadius: '8px',
                background: '#fef2f2',
                border: '1.5px solid #fecaca',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '10px'
              }}>
                <AlertTriangle size={18} color="#ef4444" style={{ marginTop: '2px', flexShrink: 0 }} />
                <div>
                  <strong style={{ fontSize: '13px', color: '#b91c1c' }}>Flagged for Bad Practice:</strong>
                  <p style={{ fontSize: '12px', color: '#b91c1c', marginTop: '2px', fontWeight: 500 }}>
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
              borderTop: '1px solid #f1f5f9',
              borderBottom: '1px solid #f1f5f9',
              fontSize: '13px',
              color: '#334155'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <img
                  src={video.mentorAvatar}
                  alt={video.mentorName}
                  style={{ width: '24px', height: '24px', borderRadius: '50%', border: '1px solid #e2e8f0' }}
                />
                <span>By <strong style={{ color: '#09090b' }}>{video.mentorName}</strong></span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontWeight: 600 }}>
                <Eye size={15} />
                <span>{video.views.toLocaleString()} Views</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#64748b', fontWeight: 600 }}>
                <ThumbsUp size={15} />
                <span>{video.likes} Likes</span>
              </div>
              <div style={{ color: '#64748b', fontWeight: 600 }}>
                <span>Duration: <strong style={{ color: '#09090b' }}>{video.duration}</strong></span>
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
            <button className="btn btn-danger" onClick={() => { onFlag(video); onClose(); }}>
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
