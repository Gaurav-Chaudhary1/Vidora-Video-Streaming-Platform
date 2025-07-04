package com.vidora.app.data.remote.models.comment

data class Comment(
    val id: String,
    val content: String,
    val user: UserSummary,
    val createdAt: String
)

data class CommentResponseDto(
    val _id: String,
    val content: String,
    val userId: CommentUserDto,
    val createdAt: String
)

data class CommentUserDto(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String?
)

data class UserSummary(
    val id: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String?
)

data class LikeDislikeResponse(
    val likes: Int,
    val dislikes: Int,
    val liked: Boolean? = null,
    val disliked: Boolean? = null
)

fun CommentResponseDto.toCommentDomain(): Comment = Comment(
    id = _id,
    content = content,
    user = UserSummary(
        id = userId._id,
        firstName = userId.firstName,
        lastName = userId.lastName,
        avatarUrl = userId.profilePictureUrl
    ),
    createdAt = createdAt
)