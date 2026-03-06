export interface ApiResult<T> {
  code: number
  data: T
  msg?: string | null
}

export interface LoginPayload {
  uid: number
  token: string
}

export interface User {
  id: number
  name: string
  email: string
  teams?: string
}

export interface Team {
  id: number
  creatorId: number
  name: string
  teammates?: string
  infoId?: number | null
}

export interface Info {
  id?: number
  teamId?: number
  course: string
  numberLimit: number
  content: string
}

export interface Comment {
  id: number
  senderId: number
  teamId: number
  date: string
  content: string
}

export interface Application {
  id: number
  uid: number
  tid: number
  msg: string
  state: number
}

export interface TeamInfo {
  team: Team
  info: Info | null
  commentList: Comment[]
  applicationList: Application[]
}

export interface UserInfo {
  user: User
  commentList: Comment[]
  applicationList: Application[]
}

export type TabKey = 'plaza' | 'create' | 'profile'
