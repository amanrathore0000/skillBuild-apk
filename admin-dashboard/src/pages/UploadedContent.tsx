import React, { useState } from 'react';
import { UploadedCourse, UploadedVideo } from '../types/admin';
import {
  Video,
  BookOpen,
  Search,
  Filter,
  Play,
  AlertTriangle,
  CheckCircle,
  Trash2,
  Clock,
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
          <div style={{ display: 'flex', background: 'var(--bg-input)', padding: '4px', borderRadius: '10px' }}>
            <button
              onClick={() => setContentType('VIDEOS')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '8px 18px',
                borderRadius: '8px',
                border: 'none',
                background: contentType === 'VIDEOS' ? 'var(--primary)' : 'transparent',
                color: contentType === 'VIDEOS' ? '#fff' : 'var(--text-secondary)',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                transition: 'all 0.15s'
              }}
            >
              <Video size={16} />
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
                background: contentType === 'COURSES' ? 'var(--primary)' : 'transparent',
                color: contentType === 'COURSES' ? '#fff' : 'var(--text-secondary)',
                fontWeight: 700,
                fontSize: '13px',
                cursor: 'pointer',
                transition: 'all 0.15s'
              }}
            >
              <BookOpen size={16} />
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
                border: video.flaggedForBadPractice ? '1px solid rgba(239, 68, 68, 0.4)' : undefined
              }}
            >
              {/* Thumbnail Container */}
              <div
                style={{
                  position: 'relative',
                  width: '100%',
                  aspectRatio: '16/9',
                  cursor: 'pointer',
                  backgroundColor: '#000'
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
                  background: 'rgba(0, 0, 0, 0.8)',
                  padding: '3px 7px',
                  borderRadius: '4px',
                  fontSize: '11px',
                  fontWeight: 700,
                  color: '#fff'
                }}>
                  {video.duration}
                </span>

                {/* Status Badge Top Left */}
                <div style={{ position: 'absolute', top: '8px', left: '8px' }}>
                  <span className={`badge badge-${video.status === 'APPROVED' ? 'active' : video.status === 'FLAGGED' ? 'warned' : 'blocked'}`}>
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
                  opacity: 0.85,
                  transition: 'opacity 0.2s'
                }}>
                  <div style={{
                    width: '46px',
                    height: '46px',
                    borderRadius: '50%',
                    background: 'rgba(99, 102, 241, 0.85)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 14px rgba(0,0,0,0.5)'
                  }}>
                    <Play size={20} color="#fff" style={{ marginLeft: '2px' }} />
                  </div>
                </div>
              </div>

              {/* Card Body */}
              <div style={{ padding: '16px', flex: 1, display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-light)', textTransform: 'uppercase' }}>
                  {video.category}
                </span>
                <h4 style={{
                  fontSize: '15px',
                  fontWeight: 700,
                  marginTop: '4px',
                  lineHeight: '1.4',
                  color: 'var(--text-primary)',
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden'
                }}>
                  {video.title}
                </h4>

                <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Course: {video.courseTitle}
                </p>

                {video.flaggedForBadPractice && (
                  <div style={{
                    marginTop: '10px',
                    padding: '8px 10px',
                    borderRadius: '6px',
                    background: 'rgba(239, 68, 68, 0.12)',
                    fontSize: '11px',
                    color: '#fca5a5'
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
                  paddingTop: '12px',
                  borderTop: '1px solid var(--border-subtle)',
                  fontSize: '12px',
                  color: 'var(--text-secondary)'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <img src={video.mentorAvatar} alt={video.mentorName} style={{ width: '22px', height: '22px', borderRadius: '50%' }} />
                    <span style={{ fontWeight: 600 }}>{video.mentorName}</span>
                  </div>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <span><Eye size={12} style={{ verticalAlign: 'middle' }} /> {video.views}</span>
                    <span><ThumbsUp size={12} style={{ verticalAlign: 'middle' }} /> {video.likes}</span>
                  </div>
                </div>

                {/* Moderation Action Buttons */}
                <div style={{ display: 'flex', gap: '8px', marginTop: '14px' }}>
                  <button
                    className="btn btn-outline btn-sm"
                    style={{ flex: 1 }}
                    onClick={() => onInspectVideo(video)}
                  >
                    <Play size={13} />
                    Inspect Player
                  </button>

                  {video.status !== 'APPROVED' && (
                    <button
                      className="btn btn-success btn-sm"
                      title="Approve Video"
                      onClick={() => onApproveVideo(video)}
                    >
                      <CheckCircle size={13} />
                    </button>
                  )}

                  {video.status !== 'FLAGGED' && (
                    <button
                      className="btn btn-warning btn-sm"
                      title="Flag as Bad Practice"
                      onClick={() => onFlagVideo(video)}
                    >
                      <AlertTriangle size={13} />
                    </button>
                  )}

                  <button
                    className="btn btn-danger btn-sm"
                    title="Remove Video"
                    onClick={() => onRemoveVideo(video)}
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
            <div key={course.id} className="glass-card" style={{ overflow: 'hidden' }}>
              <div style={{ position: 'relative', width: '100%', height: '160px' }}>
                <img src={course.thumbnailUrl} alt={course.title} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                <div style={{ position: 'absolute', top: '10px', left: '10px' }}>
                  <span className={`badge badge-${course.status === 'APPROVED' ? 'active' : 'warned'}`}>
                    {course.status}
                  </span>
                </div>
                <div style={{
                  position: 'absolute',
                  top: '10px',
                  right: '10px',
                  background: 'var(--primary)',
                  color: '#fff',
                  fontWeight: 800,
                  fontSize: '12px',
                  padding: '3px 8px',
                  borderRadius: '6px'
                }}>
                  {course.price}
                </div>
              </div>

              <div style={{ padding: '18px' }}>
                <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-light)' }}>
                  {course.category}
                </span>
                <h4 style={{ fontSize: '16px', fontWeight: 800, marginTop: '4px', color: 'var(--text-primary)' }}>
                  {course.title}
                </h4>

                {course.flagReason && (
                  <div style={{
                    marginTop: '8px',
                    padding: '8px',
                    borderRadius: '6px',
                    background: 'rgba(239, 68, 68, 0.12)',
                    fontSize: '12px',
                    color: '#fca5a5'
                  }}>
                    ⚠ {course.flagReason}
                  </div>
                )}

                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginTop: '16px',
                  fontSize: '12px',
                  color: 'var(--text-secondary)'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <img src={course.mentorAvatar} alt={course.mentorName} style={{ width: '24px', height: '24px', borderRadius: '50%' }} />
                    <span>{course.mentorName}</span>
                  </div>
                  <span>{course.lessonsCount} Lessons • {course.duration}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
