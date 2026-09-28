import React, { useState } from 'react';
import { UploadedCourse, UploadedVideo } from '../types/admin';
import {
  Video,
  BookOpen,
  Search,
  Play,
  AlertTriangle,
  CheckCircle,
  Trash2,
  Eye,
  ThumbsUp
} from 'lucide-react';

interface UploadedContentProps {
  courses: UploadedCourse[];
  videos: UploadedVideo[];
  onInspectVideo: (video: UploadedVideo) => void;
  onFlagVideo: (video: UploadedVideo) => void;
  onRemoveVideo: (video: UploadedVideo) => void;
  onApproveVideo: (video: UploadedVideo) => void;
}

export const UploadedContent: React.FC<UploadedContentProps> = ({
  courses,
  videos,
  onInspectVideo,
  onFlagVideo,
  onRemoveVideo,
  onApproveVideo
}) => {
  const [contentType, setContentType] = useState<'VIDEOS' | 'COURSES'>('VIDEOS');
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  const categories = ['ALL', 'Tech & Coding', 'Music', 'Culinary Arts', 'Design & Art', 'Languages'];

  const filteredVideos = videos.filter(v => {
    const matchesSearch = v.title.toLowerCase().includes(search.toLowerCase()) ||
                          v.mentorName.toLowerCase().includes(search.toLowerCase()) ||
                          v.courseTitle.toLowerCase().includes(search.toLowerCase());
    const matchesCat = categoryFilter === 'ALL' || v.category === categoryFilter;
    return matchesSearch && matchesCat;
  });

  const filteredCourses = courses.filter(c => {
    const matchesSearch = c.title.toLowerCase().includes(search.toLowerCase()) ||
                          c.mentorName.toLowerCase().includes(search.toLowerCase());
    const matchesCat = categoryFilter === 'ALL' || c.category === categoryFilter;
    return matchesSearch && matchesCat;
  });

  return (
    <div>
      {/* Top Toggle & Filters */}
      <div className="glass-card" style={{ padding: '20px 24px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          {/* Sub-tabs: Video Lessons vs Courses */}
          <div style={{ display: 'flex', background: '#f1f5f9', border: '1px solid #e2e8f0', padding: '4px', borderRadius: '10px' }}>
            <button
              onClick={() => setContentType('VIDEOS')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 18px',
                borderRadius: '8px',
                border: 'none',
                background: contentType === 'VIDEOS' ? '#09090b' : 'transparent',
                color: contentType === 'VIDEOS' ? '#ffffff' : '#475569',
                fontWeight: 800,
                fontSize: '13px',
                cursor: 'pointer',
                transition: 'all 0.15s'
              }}
            >
              <Video size={16} color={contentType === 'VIDEOS' ? '#10b981' : 'currentColor'} />
              Uploaded Videos ({videos.length})
            </button>

            <button
              onClick={() => setContentType('COURSES')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 18px',
                borderRadius: '8px',
                border: 'none',
                background: contentType === 'COURSES' ? '#09090b' : 'transparent',
                color: contentType === 'COURSES' ? '#ffffff' : '#475569',
                fontWeight: 800,
                fontSize: '13px',
                cursor: 'pointer',
                transition: 'all 0.15s'
              }}
            >
              <BookOpen size={16} color={contentType === 'COURSES' ? '#10b981' : 'currentColor'} />
              Courses on Server ({courses.length})
            </button>
          </div>

          {/* Search and Category Filter */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
            <div className="search-input-wrapper" style={{ width: '280px' }}>
              <Search size={15} />
              <input
                type="text"
                className="search-input"
                placeholder="Search uploads..."
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>

            <select
              className="select-input"
              value={categoryFilter}
              onChange={e => setCategoryFilter(e.target.value)}
            >
              {categories.map(cat => (
                <option key={cat} value={cat}>{cat === 'ALL' ? 'All Categories' : cat}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Videos View */}
      {contentType === 'VIDEOS' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '22px' }}>
          {filteredVideos.map(video => (
            <div
              key={video.id}
              className="glass-card"
              style={{
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column',
                backgroundColor: '#ffffff',
                border: video.flaggedForBadPractice ? '1.5px solid #fecaca' : '1px solid #e2e8f0'
              }}
            >
              {/* Thumbnail Container */}
              <div
                style={{
                  position: 'relative',
                  width: '100%',
                  aspectRatio: '16/9',
                  cursor: 'pointer',
                  backgroundColor: '#09090b'
                }}
                onClick={() => onInspectVideo(video)}
              >
                <img
                  src={video.thumbnailUrl}
                  alt={video.title}
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />

                {/* Duration Overlay */}
                <span style={{
                  position: 'absolute',
                  bottom: '8px',
                  right: '8px',
                  background: 'rgba(9, 9, 11, 0.85)',
                  padding: '3px 8px',
                  borderRadius: '4px',
                  fontSize: '11px',
                  fontWeight: 800,
                  color: '#ffffff'
                }}>
                  {video.duration}
                </span>

                {/* Status Badge Top Left */}
                <div style={{ position: 'absolute', top: '8px', left: '8px' }}>
                  <span className={`badge badge-${video.status === 'APPROVED' ? 'active' : 'danger'}`}>
                    {video.status}
                  </span>
                </div>

                {/* Center Play Button Overlay */}
                <div style={{
                  position: 'absolute',
                  inset: 0,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: 'rgba(0, 0, 0, 0.25)',
                  opacity: 0.9,
                  transition: 'opacity 0.2s'
                }}>
                  <div style={{
                    width: '46px',
                    height: '46px',
                    borderRadius: '50%',
                    background: 'linear-gradient(135deg, #10b981, #059669)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 14px rgba(16, 185, 129, 0.5)'
                  }}>
                    <Play size={20} color="#ffffff" style={{ marginLeft: '2px' }} />
                  </div>
                </div>
              </div>

              {/* Card Body */}
              <div style={{ padding: '18px', flex: 1, display: 'flex', flexDirection: 'column', backgroundColor: '#ffffff' }}>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#10b981', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  {video.category}
                </span>
                <h4 style={{
                  fontSize: '15px',
                  fontWeight: 800,
                  marginTop: '4px',
                  lineHeight: '1.4',
                  color: '#09090b',
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden'
                }}>
                  {video.title}
                </h4>

                <p style={{ fontSize: '12px', color: '#64748b', marginTop: '4px', fontWeight: 500 }}>
                  Course: {video.courseTitle}
                </p>

                {video.flaggedForBadPractice && (
                  <div style={{
                    marginTop: '10px',
                    padding: '8px 12px',
                    borderRadius: '6px',
                    background: '#fef2f2',
                    border: '1px solid #fecaca',
                    fontSize: '11px',
                    fontWeight: 600,
                    color: '#b91c1c'
                  }}>
                    ⚠ {video.flagReason}
                  </div>
                )}

                {/* Mentor & Stats */}
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginTop: 'auto',
                  paddingTop: '14px',
                  borderTop: '1px solid #f1f5f9',
                  fontSize: '12px',
                  color: '#334155'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <img src={video.mentorAvatar} alt={video.mentorName} style={{ width: '24px', height: '24px', borderRadius: '50%', border: '1px solid #e2e8f0' }} />
                    <span style={{ fontWeight: 700, color: '#09090b' }}>{video.mentorName}</span>
                  </div>

                  <div style={{ display: 'flex', gap: '10px', color: '#64748b', fontWeight: 600 }}>
                    <span><Eye size={12} style={{ verticalAlign: 'middle', marginRight: '3px' }} /> {video.views}</span>
                    <span><ThumbsUp size={12} style={{ verticalAlign: 'middle', marginRight: '3px' }} /> {video.likes}</span>
                  </div>
                </div>

                {/* Moderation Action Buttons */}
                <div style={{ display: 'flex', gap: '8px', marginTop: '16px' }}>
                  <button
                    className="btn btn-outline btn-sm"
                    style={{ flex: 1 }}
                    onClick={() => onInspectVideo(video)}
                  >
                    <Play size={13} color="#09090b" />
                    Inspect Player
                  </button>

                  {video.status !== 'APPROVED' && (
                    <button
                      className="btn btn-success btn-sm"
                      title="Approve Video"
                      onClick={() => onApproveVideo(video)}
                    >
                      <CheckCircle size={13} />
                      Approve
                    </button>
                  )}

                  {video.status !== 'FLAGGED' && (
                    <button
                      className="btn btn-danger btn-sm"
                      title="Flag as Bad Practice"
                      onClick={() => onFlagVideo(video)}
                    >
                      <AlertTriangle size={13} />
                      Flag
                    </button>
                  )}

                  <button
                    className="btn btn-danger btn-sm"
                    title="Remove Video"
                    onClick={() => onRemoveVideo(video)}
                    style={{ padding: '6px 10px' }}
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Courses View */}
      {contentType === 'COURSES' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '22px' }}>
          {filteredCourses.map(course => (
            <div key={course.id} className="glass-card" style={{ overflow: 'hidden', backgroundColor: '#ffffff' }}>
              <div style={{ position: 'relative', width: '100%', height: '160px' }}>
                <img src={course.thumbnailUrl} alt={course.title} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                <div style={{ position: 'absolute', top: '10px', left: '10px' }}>
                  <span className={`badge badge-${course.status === 'APPROVED' ? 'active' : 'danger'}`}>
                    {course.status}
                  </span>
                </div>
                <div style={{
                  position: 'absolute',
                  top: '10px',
                  right: '10px',
                  background: '#10b981',
                  color: '#ffffff',
                  fontWeight: 800,
                  fontSize: '12px',
                  padding: '3px 10px',
                  borderRadius: '6px',
                  boxShadow: '0 2px 6px rgba(0, 0, 0, 0.2)'
                }}>
                  {course.price}
                </div>
              </div>

              <div style={{ padding: '18px', backgroundColor: '#ffffff' }}>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#10b981', letterSpacing: '0.5px' }}>
                  {course.category}
                </span>
                <h4 style={{ fontSize: '16px', fontWeight: 800, marginTop: '4px', color: '#09090b' }}>
                  {course.title}
                </h4>

                {course.flagReason && (
                  <div style={{
                    marginTop: '8px',
                    padding: '8px 12px',
                    borderRadius: '6px',
                    background: '#fef2f2',
                    border: '1px solid #fecaca',
                    fontSize: '12px',
                    fontWeight: 600,
                    color: '#b91c1c'
                  }}>
                    ⚠ {course.flagReason}
                  </div>
                )}

                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginTop: '16px',
                  paddingTop: '12px',
                  borderTop: '1px solid #f1f5f9',
                  fontSize: '12px',
                  color: '#475569'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <img src={course.mentorAvatar} alt={course.mentorName} style={{ width: '24px', height: '24px', borderRadius: '50%', border: '1px solid #e2e8f0' }} />
                    <span style={{ fontWeight: 700, color: '#09090b' }}>{course.mentorName}</span>
                  </div>
                  <span style={{ fontWeight: 600 }}>{course.lessonsCount} Lessons • {course.duration}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
