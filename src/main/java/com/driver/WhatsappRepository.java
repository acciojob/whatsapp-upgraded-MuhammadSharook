package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class WhatsappRepository {

    HashMap<String,User> user_db = new HashMap<>();
    HashMap<Group,List<User>> group_db = new HashMap<>();
    HashMap<Group,User> admin_db = new HashMap<>();
    HashMap<Message,User> sender_message_db = new HashMap<>();
    HashMap<Group,List<Message>> group_message_db = new HashMap<>();
    HashMap<Date,Message> message_db = new HashMap<>();
    int Group_Count;
    int Message_Id = 0;

    public String createUser(String name, String mobile) {
        User user = user_db.get(mobile);
        if(user != null)
        {
            throw new RuntimeException("User already exists");
        }

        User newuser = new User();
        newuser.setName(name);
        newuser.setMobile(mobile);
        user_db.put(mobile,newuser);

        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        if(users.size() < 2)
        {
            throw new RuntimeException("Users must be 2 or more");
        } else if (users.size() == 2) {
            User user = users.get(1);

            Group group = new Group();
            group.setName(user.getName());
            group.setNumberOfParticipants(2);

            admin_db.put(group,users.get(0));
            group_db.put(group,users);

            return group;
        }
        else {
            Group_Count++;
            Group group = new Group();
            group.setName("Group" + Group_Count);
            group.setNumberOfParticipants(users.size());

            group_db.put(group,users);
            admin_db.put(group,users.get(0));

            return group;
        }
    }

    public int createMessage(String content) {
        Message message = new Message();
        message.setId(Message_Id);
        message.setContent(content);
        Date date = new Date();
        message.setTimestamp(date);

        message_db.put(date,message);

        Message_Id++;

        return Message_Id;
    }

    public int sendMessage(Message message, User sender, Group group) {
        int no_messages = 0;
        if(!group_db.containsKey(group))throw new RuntimeException("Group does not exist");
        else if (!group_db.get(group).contains(sender)) {
            throw new RuntimeException("You are not allowed to send message");
        }
        else {
            List<Message> messageList = new ArrayList<>();
            if(group_message_db.containsKey(group))messageList = group_message_db.get(group);

            messageList.add(message);
            group_message_db.put(group,messageList);
            sender_message_db.put(message,sender);
            no_messages = messageList.size();

            return no_messages;
        }
    }

    public String changeAdmin(User approver, User user, Group group) {
        if(!group_db.containsKey(group))throw new RuntimeException("Group does not exist");
        else if (!admin_db.containsKey(approver)) {
            throw new RuntimeException("Approver does not have rights");
        } else if (!group_db.get(group).contains(user)) {
            throw new RuntimeException("User is not a participant");
        }
        else {
            admin_db.put(group,user);

            return "SUCCESS";
        }
    }

    public int removeUser(User user) {
        boolean user_flag = false;
        Group User_Group = null;

        for(Group group : group_db.keySet())
        {
            List<User> userList = group_db.get(group);
            if(userList.contains(user))
            {
                user_flag = true;
                User_Group = group;
                break;
            }
        }

        if(!user_flag)
        {
            throw new RuntimeException("User not found");
        }

        if(admin_db.get(User_Group) == user)
        {
            throw new RuntimeException("Cannot remove admin");
        }

        group_db.get(User_Group).remove(user);

        List<Message> messages_removed_list = new ArrayList<>();

        for(Message message : sender_message_db.keySet())
        {
            if(sender_message_db.get(message) == user)
            {
                messages_removed_list.add(message);
            }
        }

        for(Message message : messages_removed_list)
        {
            group_message_db.get(User_Group).remove(message);
            sender_message_db.remove(message);
        }

        user_db.remove(user);
        User_Group.setNumberOfParticipants(group_db.get(User_Group).size());

        return group_db.get(User_Group).size() + group_message_db.get(User_Group).size() + sender_message_db.size();
    }

    public String findMessage(Date start, Date end, int k) {
        int no_messages = 0;
        for(Date date : message_db.keySet())
        {
            if(date.getTime() > start.getTime() && date.getTime() < end.getTime())
            {
                no_messages++;
            }
        }

        if(k > no_messages)
        {
            throw new RuntimeException("K is greater than the number of messages");
        }
        else
        {
            return "Number of Messages are" + no_messages;
        }
    }
}
